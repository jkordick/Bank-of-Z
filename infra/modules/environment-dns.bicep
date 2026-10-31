@description('Container Apps environment domain resolved after environment creation.')
param defaultDomain string
@description('Internal environment ingress IPv4 address.')
param staticIp string
@description('Resource tags.')
param tags object
@description('Spoke VNet ID.')
param vnetId string
@description('Existing hub VNet ID used by enterprise DNS resolvers.')
param hubVnetId string

resource zone 'Microsoft.Network/privateDnsZones@2024-06-01' = {
  name: defaultDomain
  location: 'global'
  tags: tags
}

var linkedVnets = [
  vnetId
  hubVnetId
]
var recordNames = [
  '*'
  '@'
]

resource links 'Microsoft.Network/privateDnsZones/virtualNetworkLinks@2024-06-01' = [for (id, index) in linkedVnets: {
  parent: zone
  name: 'network-${index}'
  location: 'global'
  properties: {
    virtualNetwork: { id: id }
    registrationEnabled: false
  }
}]

resource records 'Microsoft.Network/privateDnsZones/A@2024-06-01' = [for recordName in recordNames: {
  parent: zone
  name: recordName
  properties: {
    TTL: 300
    aRecords: [{ ipv4Address: staticIp }]
  }
}]
