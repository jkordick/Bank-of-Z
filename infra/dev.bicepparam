using './main.bicep'

param location = readEnvironmentVariable('BANKZ_LOCATION')
param environmentName = 'dev'
param hubSubscriptionId = readEnvironmentVariable('BANKZ_HUB_SUBSCRIPTION_ID')
param hubResourceGroupName = readEnvironmentVariable('BANKZ_HUB_RESOURCE_GROUP')
param hubVnetName = readEnvironmentVariable('BANKZ_HUB_VNET_NAME')
param dnsSubscriptionId = readEnvironmentVariable('BANKZ_DNS_SUBSCRIPTION_ID')
param dnsResourceGroupName = readEnvironmentVariable('BANKZ_DNS_RESOURCE_GROUP')
param monitorSubscriptionId = readEnvironmentVariable('BANKZ_MONITOR_SUBSCRIPTION_ID')
param monitorResourceGroupName = readEnvironmentVariable('BANKZ_MONITOR_RESOURCE_GROUP')
param monitorPrivateLinkScopeName = readEnvironmentVariable('BANKZ_MONITOR_PRIVATE_LINK_SCOPE_NAME')
param spokeAddressPrefix = readEnvironmentVariable('BANKZ_SPOKE_CIDR')
param appsSubnetPrefix = readEnvironmentVariable('BANKZ_APPS_SUBNET_CIDR')
param endpointsSubnetPrefix = readEnvironmentVariable('BANKZ_ENDPOINTS_SUBNET_CIDR')
param dnsServers = json(readEnvironmentVariable('BANKZ_DNS_SERVERS_JSON'))
param mainframeAddressPrefixes = json(readEnvironmentVariable('BANKZ_MAINFRAME_CIDRS_JSON'))
param zosConnectAddressPrefixes = json(readEnvironmentVariable('BANKZ_ZOS_CONNECT_CIDRS_JSON'))
param zosConnectUrlSecretName = 'zos-connect-base-url-dev'
param zosConnectCredentialSecretName = 'zos-connect-credential-dev'
param applicationInsightsSecretName = 'applicationinsights-connection-string-dev'
param minReplicas = 1
param maxReplicas = 3
