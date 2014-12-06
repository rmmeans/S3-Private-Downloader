package utils

import com.amazonaws.auth.{AWSCredentials, AWSCredentialsProvider}
import play.api.Configuration

class TypeSafeConfigAWSCredentialsProvider(config: Configuration) extends AWSCredentialsProvider {
  override def getCredentials: AWSCredentials = {
    new AWSCredentials {
      override def getAWSAccessKeyId: String = config.getString("aws.accessKey").getOrElse(throw new Exception("you forgot some config man!"))

      override def getAWSSecretKey: String = config.getString("aws.secretKey").getOrElse(throw new Exception("you forgot some config man!"))
    }
  }

  override def refresh(): Unit = () //nothing to do here.
}
