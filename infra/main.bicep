targetScope = 'resourceGroup'

@description('Azure region accepted by the platform owner; must support zone-redundant Container Apps.')
param location string

@description('Environment identity; security and topology are identical across environments.')
@allowed([
  'dev'
  'staging'
  'production'
])
param environmentName string

@description('Existing connectivity hub subscription ID.')
param hubSubscriptionId string

@description('Existing connectivity hub resource group.')
param hubResourceGroupName string

@description('Existing hub VNet with a VPN/ExpressRoute gateway and gateway transit support.')
param hubVnetName string

@description('Existing shared private DNS subscription ID.')
param dnsSubscriptionId string

@description('Existing shared private DNS resource group containing ACR, Key Vault and Azure Monitor zones.')
param dnsResourceGroupName string

@description('Existing shared Azure Monitor Private Link Scope subscription ID.')
param monitorSubscriptionId string

@description('Existing shared Azure Monitor Private Link Scope resource group.')
param monitorResourceGroupName string

@description('Existing shared Azure Monitor Private Link Scope name; one scope per interconnected DNS domain.')
param monitorPrivateLinkScopeName string

@description('Unused, non-overlapping spoke IPv4 CIDR allocated by the network owner.')
param spokeAddressPrefix string

@description('Dedicated Container Apps subnet CIDR, /23 or larger, within the spoke.')
param appsSubnetPrefix string

@description('Private endpoint subnet CIDR within the spoke, not overlapping the apps subnet.')
param endpointsSubnetPrefix string

@description('Reachable enterprise DNS resolver IPv4 addresses; resolvers must resolve Azure private zones and z/OS Connect.')
@minLength(1)
param dnsServers array

@description('Mainframe IPv4 CIDRs; all access except the explicit z/OS Connect HTTPS rule is denied.')
@minLength(1)
param mainframeAddressPrefixes array

@description('z/OS Connect IPv4 addresses/CIDRs permitted over HTTPS; must be within the mainframe CIDRs.')
@minLength(1)
param zosConnectAddressPrefixes array

@description('Key Vault secret name containing the accepted HTTPS z/OS Connect base URL.')
param zosConnectUrlSecretName string

@description('Key Vault secret name containing the credential consumed by the future accepted identity adapter.')
param zosConnectCredentialSecretName string

@description('Key Vault secret name containing this environment Application Insights connection string.')
param applicationInsightsSecretName string

@description('Create the application only after the REST/identity/read-path/instrumentation gates are accepted.')
param deployApplication bool = false

@description('Repository in the generated ACR containing the approved instrumented Java/Spring Boot image.')
param imageRepository string = 'inqacc'

@description('Immutable sha256 image digest; required by the application module when enabled.')
param imageDigest string = ''

@description('Minimum always-ready application replicas.')
@minValue(1)
param minReplicas int = 1

@description('Maximum application replicas; must be greater than or equal to minReplicas.')
@minValue(1)
@maxValue(100)
param maxReplicas int = 3

var suffix = uniqueString(subscription().subscriptionId, resourceGroup().id)
var prefix = 'bankz-${environmentName}'
var tags = {
  workload: 'bank-of-z-inqacc'
  environment: environmentName
  managedBy: 'bicep'
}
var hubVnetId = resourceId(hubSubscriptionId, hubResourceGroupName, 'Microsoft.Network/virtualNetworks', hubVnetName)
var monitorPrivateLinkScopeId = resourceId(monitorSubscriptionId, monitorResourceGroupName, 'Microsoft.Insights/privateLinkScopes', monitorPrivateLinkScopeName)

module network './modules/network.bicep' = {
  name: '${prefix}-network'
  params: {
    location: location
    prefix: prefix
    tags: tags
    spokeAddressPrefix: spokeAddressPrefix
    appsSubnetPrefix: appsSubnetPrefix
    endpointsSubnetPrefix: endpointsSubnetPrefix
    dnsServers: dnsServers
    mainframeAddressPrefixes: mainframeAddressPrefixes
    zosConnectAddressPrefixes: zosConnectAddressPrefixes
  }
}

module hubConnection './modules/hub-connection.bicep' = {
  name: '${prefix}-hub-connection'
  scope: resourceGroup(hubSubscriptionId, hubResourceGroupName)
  params: {
    hubVnetName: hubVnetName
    spokeVnetId: network.outputs.vnetId
    peeringName: '${prefix}-spoke'
  }
}

resource spokeVnet 'Microsoft.Network/virtualNetworks@2024-05-01' existing = {
  name: '${prefix}-vnet'
}

resource spokePeering 'Microsoft.Network/virtualNetworks/virtualNetworkPeerings@2024-05-01' = {
  parent: spokeVnet
  name: 'connectivity-hub'
  properties: {
    remoteVirtualNetwork: { id: hubVnetId }
    allowVirtualNetworkAccess: true
    allowForwardedTraffic: true
    allowGatewayTransit: false
    useRemoteGateways: true
  }
  dependsOn: [hubConnection]
}

module dnsLinks './modules/dns-links.bicep' = {
  name: '${prefix}-dns-links'
  scope: resourceGroup(dnsSubscriptionId, dnsResourceGroupName)
  params: {
    spokeVnetId: network.outputs.vnetId
    linkName: '${prefix}-${suffix}'
  }
}

module monitoring './modules/monitoring.bicep' = {
  name: '${prefix}-monitoring'
  params: {
    location: location
    prefix: prefix
    tags: tags
  }
}

module services './modules/services.bicep' = {
  name: '${prefix}-services'
  params: {
    location: location
    prefix: prefix
    suffix: suffix
    tags: tags
    workspaceId: monitoring.outputs.workspaceId
    applicationInsightsId: monitoring.outputs.applicationInsightsId
  }
}

module registryEndpoint './modules/private-endpoint.bicep' = {
  name: '${prefix}-registry-endpoint'
  params: {
    location: location
    name: '${prefix}-acr-pe'
    tags: tags
    subnetId: network.outputs.endpointsSubnetId
    serviceId: services.outputs.registryId
    groupId: 'registry'
    privateDnsZoneIds: [
      resourceId(dnsSubscriptionId, dnsResourceGroupName, 'Microsoft.Network/privateDnsZones', 'privatelink.azurecr.io')
    ]
  }
}

module vaultEndpoint './modules/private-endpoint.bicep' = {
  name: '${prefix}-vault-endpoint'
  params: {
    location: location
    name: '${prefix}-kv-pe'
    tags: tags
    subnetId: network.outputs.endpointsSubnetId
    serviceId: services.outputs.vaultId
    groupId: 'vault'
    privateDnsZoneIds: [
      resourceId(dnsSubscriptionId, dnsResourceGroupName, 'Microsoft.Network/privateDnsZones', 'privatelink.vaultcore.azure.net')
    ]
  }
}

module monitorConnection './modules/monitor-connection.bicep' = {
  name: '${prefix}-monitor-connection'
  scope: resourceGroup(monitorSubscriptionId, monitorResourceGroupName)
  params: {
    privateLinkScopeName: monitorPrivateLinkScopeName
    linkPrefix: '${prefix}-${suffix}'
    workspaceId: monitoring.outputs.workspaceId
    applicationInsightsId: monitoring.outputs.applicationInsightsId
  }
}

module monitorEndpoint './modules/private-endpoint.bicep' = {
  name: '${prefix}-monitor-endpoint'
  params: {
    location: location
    name: '${prefix}-monitor-pe'
    tags: tags
    subnetId: network.outputs.endpointsSubnetId
    serviceId: monitorPrivateLinkScopeId
    groupId: 'azuremonitor'
    privateDnsZoneIds: [for zoneName in [
      'privatelink.monitor.azure.com'
      'privatelink.oms.opinsights.azure.com'
      'privatelink.ods.opinsights.azure.com'
      'privatelink.agentsvc.azure-automation.net'
      'privatelink.blob.core.windows.net'
    ]: resourceId(dnsSubscriptionId, dnsResourceGroupName, 'Microsoft.Network/privateDnsZones', zoneName)]
  }
  dependsOn: [monitorConnection]
}

module environment './modules/environment.bicep' = {
  name: '${prefix}-environment'
  params: {
    location: location
    prefix: prefix
    tags: tags
    appsSubnetId: network.outputs.appsSubnetId
    vnetId: network.outputs.vnetId
    hubVnetId: hubVnetId
    workspaceId: monitoring.outputs.workspaceId
  }
  dependsOn: [
    spokePeering
    dnsLinks
  ]
}

module application './modules/application.bicep' = if (deployApplication) {
  name: '${prefix}-application'
  params: {
    location: location
    prefix: prefix
    tags: tags
    environmentId: environment.outputs.environmentId
    identityId: services.outputs.identityId
    identityClientId: services.outputs.identityClientId
    registryServer: services.outputs.registryServer
    vaultUri: services.outputs.vaultUri
    zosConnectUrlSecretName: zosConnectUrlSecretName
    zosConnectCredentialSecretName: zosConnectCredentialSecretName
    applicationInsightsSecretName: applicationInsightsSecretName
    imageRepository: imageRepository
    imageDigest: imageDigest
    minReplicas: minReplicas
    maxReplicas: maxReplicas
  }
  dependsOn: [
    registryEndpoint
    vaultEndpoint
    monitorEndpoint
  ]
}

output registryServer string = services.outputs.registryServer
output vaultUri string = services.outputs.vaultUri
output identityId string = services.outputs.identityId
output workspaceId string = monitoring.outputs.workspaceId
output applicationInsightsId string = monitoring.outputs.applicationInsightsId
output environmentId string = environment.outputs.environmentId
output internalDomain string = environment.outputs.defaultDomain
output internalIp string = environment.outputs.staticIp
output applicationEnabled bool = deployApplication
