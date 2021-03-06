provider "azurerm" {
  version = "1.22.1"
}

data "azurerm_key_vault" "sscs_key_vault" {
  name                = "${local.azureVaultName}"
  resource_group_name = "${local.azureVaultName}"
}

locals {
  azureVaultName = "sscs-${var.env}"
}
