@description('Existing hub VNet name; no gateway or circuit is created.')
param hubVnetName string
@description('New spoke VNet resource ID.')
param spokeVnetId string
@description('New hub-side peering name.')
param peeringName string

resource hub 'Microsoft.Network/virtualNetworks@2024-05-01' existing = {
  name: hubVnetName
}

resource peering 'Microsoft.Network/virtualNetworks/virtualNetworkPeerings@2024-05-01' = {
  parent: hub
  name: peeringName
  properties: {
    remoteVirtualNetwork: { id: spokeVnetId }
    allowVirtualNetworkAccess: true
    allowForwardedTraffic: true
    allowGatewayTransit: true
    useRemoteGateways: false
  }
}
