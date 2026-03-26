// def call(Map config = [:], Closure body) {
def call(Map config = [:], String extraContainers = "") {  
  def mavenImage  = config.mavenImage ?: "maven:3.9.6-eclipse-temurin-17"
  def buildahImage = config.buildahImage ?: "quay.io/buildah/stable:v1.34"
  def toolImage   = config.toolImage ?: "if-no-image-passed"
  return """
apiVersion: v1
kind: Pod
spec:
  # force EC2 nodes
  nodeSelector:
    role: jenkins-agent   #If you want pods to get scheduled in Karpenter nodes only.

  volumes:
    - name: maven-settings
      configMap:
        name: maven-settings
        items:
          - key: settings.xml
            path: settings.xml

    - name: maven-repo-cache
      emptyDir: {}

  serviceAccountName: jenkins-deployer
  containers:
  - name: maven
    image: ${mavenImage}
    command: ["cat"]
    tty: true
    volumeMounts:
      - name: maven-settings
        mountPath: /root/.m2/settings.xml
        subPath: settings.xml
        readOnly: true
      - name: maven-repo-cache
        mountPath: /root/.m2/repository

  - name: buildah
    image: ${buildahImage}
    securityContext:
      privileged: true
    command: ["cat"]
    tty: true      
    env:
      - name: AWS_REGION
        value: "us-east-1"

  - name: jnlp
    image: jenkins/inbound-agent:3355.v388858a_47b_33-7
    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
  - name: tools
    image: ${toolImage}
    imagePullPolicy: Always
    command:
    - cat
    tty: true    
    env:
      - name: AWS_REGION
        value: "us-east-1"     

"""
}