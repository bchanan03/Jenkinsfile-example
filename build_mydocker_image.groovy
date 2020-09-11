#!groovy

DOCKER_NAMESPACE = 'mynamespace'
def docker_image_name = "mydockerimage"
def docker_image = get_docker_name(docker_image_name)
def docker_registry = "https://mydockerregistry.com"

def registry_creds = "my_registry_cred_id_in_jenkins"
def GIT_CRED = "build_user_credentials"

pipline { 
	agent { label: 'linux' }
	options {
	  timestamps()
	  timeout(time: 30, unit: 'MINUTES')
	  ansiColor('xterm')
	}
	
	stages {  
		stage('Build artifact') {
			steps {
				script {
					echo "Build artificat here"
					image = docker.build("${docker_image}")
				}
			}
		}
		stage('Run tests') {
			steps {
				script {
					echo "Test artificat here"
				}
			}
		}
		stage('Push to Artifactory') {
			steps {
				script {
					echo "Push artificat here"
					docker.withRegistry("${docker_registry}", "${registry_creds}") {
						image.push()
					}
				}
			}
		}
		stage('Set tag') {
			when {
				beforeAgent true
				anyOf( branch "release/*" }
			}
			steps {
				withCredentials([usernamePassword(credentialsId: ${GIT_CRED}, usernameVariable: 'user', passwordVariable: 'pass') 
					sh "git config user.name "$user"
					sh "git config user.email "$user@myemail.com"
				}
				sshagent (credentials: ["${BUILD_USER_CREDNETIALS}"]) {
					sh "git tag -f -a ${GIT_BRANCH}-${BUILD_NUMBER} -m \"build ${GIT_BRANCH}-${GIT_NUMBER}\""
					sh "git push -f --tags"
				}
			}
		}
	}
	
def get_docker_name(docker_image) {
	branchType = "${env.BRANCH_NAME}".split('/')[0]
	branchName = "${env.BRANCH_NAME}".split('/')[-1]
	
	build_number = "SNAPSHOT"
	if (branchType == 'release") {
		build_number = "${BUILD_NUMBER}"
	}
	
	return "${DOCKER_NAMESPACE}/${docker_image}:${branchName}-${build_number}"
}
