
def call(Map config = [:]) {
  def toolImage  = config.toolImage ?: "if-no-image-passed"
  return """
  spec:
    serviceAccountName: jenkins-deployer
    containers:
    - name: tools
      image: ${tooImage}
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
  """
 }
 