// def call(Map config = [:], Closure body) {
def call(Map config = [:], String extraContainers = "") {  
  def mavenImage  = config.mavenImage ?: "maven:3.9.6-eclipse-temurin-17"
  def buildkitImage = config.buildkitImage ?: "ghcr.io/tonistiigi/buildkit-rootless:v0.12.5"
  def toolImage   = config.toolImage ?: "if-no-image-passed"
  return """
apiVersion: v1
kind: Pod
spec:
  volumes:
    - name: maven-settings
      configMap:
        name: maven-settings
        items:
          - key: settings.xml
            path: settings.xml

    - name: maven-repo-cache
      emptyDir: {}
    # Shared Docker auth between tools + buildkit
    - name: docker-config
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

  - name: buildkit
    image: ${buildkitImage}
    tty: true
    env:
      - name: AWS_ACCESS_KEY_ID
        valueFrom:
          secretKeyRef:
            name: aws-creds
            key: AWS_ACCESS_KEY_ID
      - name: AWS_SECRET_ACCESS_KEY
        valueFrom:
          secretKeyRef:
            name: aws-creds
            key: AWS_SECRET_ACCESS_KEY
      - name: AWS_REGION
        valueFrom:
          secretKeyRef:
            name: aws-creds
            key: AWS_REGION
    volumeMounts:
      - name: docker-config
        mountPath: /home/jenkins/.docker     

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
    - name: AWS_ACCESS_KEY_ID
      valueFrom:
        secretKeyRef:
          name: aws-creds
          key: AWS_ACCESS_KEY_ID
    - name: AWS_SECRET_ACCESS_KEY
      valueFrom:
        secretKeyRef:
          name: aws-creds
          key: AWS_SECRET_ACCESS_KEY
    - name: AWS_REGION
      valueFrom:
        secretKeyRef:
          name: aws-creds
          key: AWS_REGION 
    volumeMounts:
      - name: docker-config
        mountPath: /home/jenkins/.docker          

"""
}