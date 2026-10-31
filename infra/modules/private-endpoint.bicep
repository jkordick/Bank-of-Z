@description('Azure region.')
param location string
@description('Private endpoint name.')
param name string
@description('Resource tags.')
param tags object
@description('Private endpoint subnet ID.')
param subnetId string
@description('Private Link target resource ID.')
param serviceId string
@description('Private Link service group.')
@allowed([
  'registry'
  'vault'
  'azuremonitor'
])
param groupId string
@description('Existing shared private DNS zone resource IDs.')
@minLength(1)
param privateDnsZoneIds array

resource endpoint 'Microsoft.Network/privateEndpoints@2024-05-01' = {
  name: name
  location: location
  tags: tags
  properties: {
    subnet: { id: subnetId }
    privateLinkServiceConnections: [
      {
        name: groupId
        properties: {
          privateLinkServiceId: serviceId
          groupIds: [groupId]
        }
      }
    ]
  }
}

resource zoneGroup 'Microsoft.Network/privateEndpoints/privateDnsZoneGroups@2024-05-01' = {
  parent: endpoint
  name: 'default'
  properties: {
    privateDnsZoneConfigs: [for (zoneId, index) in privateDnsZoneIds: {
      name: 'zone-${index}'
      properties: { privateDnsZoneId: zoneId }
    }]
  }
}
