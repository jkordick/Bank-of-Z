@description('Azure region.')
param location string
@description('Environment-specific resource prefix.')
param prefix string
@description('Resource tags.')
param tags object
@description('Dedicated delegated Container Apps subnet ID.')
param appsSubnetId string
@description('Spoke VNet ID.')
param vnetId string
@description('Existing hub VNet ID used by enterprise DNS resolvers.')
param hubVnetId string
@description('Diagnostic destination workspace ID.')
param workspaceId string

resource environment 'Microsoft.App/managedEnvironments@2024-03-01' = {
  name: '${prefix}-environment'
  location: location
  tags: tags
  properties: {
    zoneRedundant: true
    vnetConfiguration: {
      infrastructureSubnetId: appsSubnetId
      internal: true
    }
    workloadProfiles: [
      {
        name: 'Consumption'
        workloadProfileType: 'Consumption'
      }
    ]
    appLogsConfiguration: {
      destination: 'azure-monitor'
    }
  }
}

resource diagnostics 'Microsoft.Insights/diagnosticSettings@2021-05-01-preview' = {
  name: 'workspace'
  scope: environment
  properties: {
    workspaceId: workspaceId
    logAnalyticsDestinationType: 'Dedicated'
    logs: [
      {
        category: 'ContainerAppConsoleLogs'
        enabled: true
      }
      {
        category: 'ContainerAppSystemLogs'
        enabled: true
      }
    ]
    metrics: [
      {
        category: 'AllMetrics'
        enabled: true
      }
    ]
  }
}

module dns './environment-dns.bicep' = {
  name: '${prefix}-environment-dns'
  params: {
    defaultDomain: environment.properties.defaultDomain
    staticIp: environment.properties.staticIp
    tags: tags
    vnetId: vnetId
    hubVnetId: hubVnetId
  }
}

output environmentId string = environment.id
output defaultDomain string = environment.properties.defaultDomain
output staticIp string = environment.properties.staticIp
