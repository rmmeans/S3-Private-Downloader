S3PrivateDownloader
===================

A simple microservice that signs the request for an S3 file and redirects you to download the file

#### How to use
Once configured, this application can redirect to any S3 object key within the pre-configured bucket.
For example, assuming my bucket name was "my-awesome-bucket" and I had a private file in there that I wished to download named: this/is/a/file/path.txt, I could
get a request for it like so: http://this-app-host:port/this/is/a/file/path.txt. This app will sign that URL and redirect your client to download it.

### Are you serious, why was this written? You wasted your time!
Yes it's overly simple to sign a URL using the SDK's Amazon provides - but take for a moment that you have private files you can't freely share and you have a ton of machines
setup in your VPC that need to be able to access those files freely - sometimes via just straight wget. Now you have a problem, because S3 doesn't allow you to 
restrict access from a VPC ID. So you are now constrained to signing every single request from every single one of your nodes, OR you use a 
simple microservice like this running on your private network inside your VPC and not exposed to the outside.


#### Instalation
This is a simple play framework application read here for details on how to run and deploy: https://www.playframework.com/documentation/2.3.x/Production
You'll need Java 8 (because we should all stop using Java 7 - that's why)

#### Configuration
Take a look at the application.conf file - this little microservice is flexable for how you want the AWS credentials provided to the application. 

Currently, the only credential loaders supported are:
* DefaultAWSCredentialsProviderChain
* ProfileCredentialsProvider 
  * you can set an additional config property "aws.profileCredentialName" to enable switching between profiles
* TypeSafeConfigAWSCredentialsProvider
  * Custom provider, allowing you to put the creds in the config file (aws.accessKey and aws.secretKey)
* InstanceProfileCredentialsProvider
* ClasspathPropertiesFileCredentialsProvider
* EnvironmentVariableCredentialsProvider
* SystemPropertiesCredentialsProvider


