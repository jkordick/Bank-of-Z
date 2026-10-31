@description('Azure region.')
param location string
@description('Environment-specific resource prefix.')
param prefix string
@description('Resource tags.')
param tags object
@description('Allocated spoke CIDR.')
param spokeAddressPrefix string
@description('Dedicated Container Apps CIDR, /23 or larger.')
param appsSubnetPrefix string
@description('Private endpoint CIDR.')
param endpointsSubnetPrefix string
@description('Enterprise DNS resolver IPv4 addresses.')
param dnsServers array
@description('Mainframe network CIDRs.')
param mainframeAddressPrefixes array
@description('Allowed z/OS Connect HTTPS destination CIDRs.')
param zosConnectAddressPrefixes array

resource appsNsg 'Microsoft.Network/networkSecurityGroups@2024-05-01' = {
  name: '${prefix}-apps-nsg'
  location: location
  tags: tags
  properties: {
    securityRules: [
      {
        name: 'Allow-ZosConnect-Https'
        properties: {
          priority: 200
          direction: 'Outbound'
          access: 'Allow'
          protocol: 'Tcp'
          sourcePortRange: '*'
          destinationPortRange: '443'
          sourceAddressPrefix: appsSubnetPrefix
          destinationAddressPrefixes: zosConnectAddressPrefixes
        }
      }
      {
        name: 'Deny-Other-Mainframe-Access'
        properties: {
          priority: 210
          direction: 'Outbound'
          access: 'Deny'
          protocol: '*'
          sourcePortRange: '*'
          destinationPortRange: '*'
          sourceAddressPrefix: appsSubnetPrefix
          destinationAddressPrefixes: mainframeAddressPrefixes
        }
      }
    ]
  }
}

resource vnet 'Microsoft.Network/virtualNetworks@2024-05-01' = {
  name: '${prefix}-vnet'
  location: location
  tags: tags
  properties: {
    addressSpace: {
      addressPrefixes: [spokeAddressPrefix]
    }
    dhcpOptions: {
      dnsServers: dnsServers
    }
    subnets: [
      {
        name: 'container-apps'
        properties: {
          addressPrefix: appsSubnetPrefix
          networkSecurityGroup: { id: appsNsg.id }
          delegations: [
            {
              name: 'container-apps'
              properties: { serviceName: 'Microsoft.App/environments' }
            }
          ]
        }
      }
      {
        name: 'private-endpoints'
        properties: {
          addressPrefix: endpointsSubnetPrefix
          privateEndpointNetworkPolicies: 'Disabled'
        }
      }
    ]
  }
}

output vnetId string = vnet.id
output appsSubnetId string = '${vnet.id}/subnets/container-apps'
output endpointsSubnetId string = '${vnet.id}/subnets/private-endpoints'
