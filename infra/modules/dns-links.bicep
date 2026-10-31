@description('New spoke VNet resource ID.')
param spokeVnetId string
@description('Unique link name in each existing private DNS zone.')
param linkName string

var zoneNames = [
  'privatelink.azurecr.io'
  'privatelink.vaultcore.azure.net'
  'privatelink.monitor.azure.com'
  'privatelink.oms.opinsights.azure.com'
  'privatelink.ods.opinsights.azure.com'
  'privatelink.agentsvc.azure-automation.net'
  'privatelink.blob.core.windows.net'
]

resource zones 'Microsoft.Network/privateDnsZones@2024-06-01' existing = [for zoneName in zoneNames: {
  name: zoneName
}]

resource links 'Microsoft.Network/privateDnsZones/virtualNetworkLinks@2024-06-01' = [for (zoneName, index) in zoneNames: {
  parent: zones[index]
  name: linkName
  location: 'global'
  properties: {
    virtualNetwork: { id: spokeVnetId }
    registrationEnabled: false
  }
}]
