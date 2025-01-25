package com.opstree.terraform

class TerraformPipeline {
    def script
    def config

    TerraformPipeline(script, config) {
        this.script = script
        this.config = config
    }

    def execute() {
        script.pipeline {
            script.agent any

            script.environment {
                TF_IN_AUTOMATION = 'true'
                TERRAFORM_VERSION = config.terraformVersion ?: '1.5.7'
                TF_VAR_ENVIRONMENT = config.environment
            }

            script.stages {
                script.stage('SCM Checkout') {
                    script.steps {
                        script.checkout scm
                    }
                }

                script.stage('Terraform Init') {
                    script.steps {
                        initTerraform()
                    }
                }

                script.stage('Terraform Validate') {
                    script.steps {
                        validateTerraform()
                    }
                }

                script.stage('Terraform Plan') {
                    script.steps {
                        planTerraform()
                    }
                }

                script.stage('Approval') {
                    script.steps {
                        script.input message: 'Approve Terraform Deployment?', 
                              parameters: [
                                  script.booleanParam(
                                      defaultValue: false, 
                                      description: 'Approve to apply Terraform changes', 
                                      name: 'APPROVE_DEPLOY'
                                  )
                              ]
                    }
                }

                script.stage('Terraform Apply') {
                    script.steps {
                        applyTerraform()
                    }
                }

                script.stage('Resource Tagging') {
                    script.steps {
                        tagResources()
                    }
                }
            }

            script.post {
                script.success {
                    notifySuccess()
                }
                script.failure {
                    notifyFailure()
                }
            }
        }
    }

    def initTerraform() {
        script.sh """
            terraform init \
            -backend-config="bucket=${config.stateBucket}" \
            -backend-config="key=${config.stateKey}" \
            -backend-config="region=${config.region}"
        """
    }

    def validateTerraform() {
        script.sh """
            terraform fmt -check
            terraform validate
        """
    }

    def planTerraform() {
        script.sh """
            terraform plan \
            -var-file=environments/${config.environment}.tfvars \
            -out=tfplan
        """
    }

    def applyTerraform() {
        script.sh 'terraform apply -auto-approve tfplan'
    }

    def tagResources() {
        def defaultTags = [
            'ManagedBy': 'Terraform',
            'Environment': config.environment,
            'Organization': 'Opstree',
            'Project': config.project ?: 'Unknown'
        ]

        def customTags = config.customTags ?: [:]
        def mergedTags = defaultTags + customTags

        script.sh """
            echo "Applying tags to infrastructure resources"
            # Add provider-specific tagging commands
        """
    }

    def notifySuccess() {
        script.echo "Terraform deployment successful for ${config.environment}"
    }

    def notifyFailure() {
        script.echo "Terraform deployment failed for ${config.environment}"
    }
}
