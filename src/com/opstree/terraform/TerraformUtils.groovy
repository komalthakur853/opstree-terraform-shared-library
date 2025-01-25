package com.opstree.terraform

class TerraformUtils {
    static def validateEnvironment(config) {
        def requiredParams = ['environment', 'stateBucket', 'stateKey', 'region']
        
        requiredParams.each { param ->
            if (!config[param]) {
                throw new Exception("Missing required parameter: ${param}")
            }
        }
    }

    static def generateTags(config) {
        def defaultTags = [
            'ManagedBy': 'Terraform',
            'Environment': config.environment,
            'Organization': 'Opstree'
        ]

        def customTags = config.customTags ?: [:]
        return defaultTags + customTags
    }

    static def sanitizeInputs(config) {
        config.terraformVersion = config.terraformVersion ?: '1.5.7'
        config.project = config.project ?: 'Unknown'
        return config
    }
}
