package com.codeUnicorn.codeUnicorn.config

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class S3Config {
    @Value("\${cloud.aws.credentials.access-key}")
    private lateinit var accessKey: String
    @Value("\${cloud.aws.credentials.secret-key}")
    private lateinit var secretKey: String
    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    @Bean
    fun amazonS3Client(): AmazonS3Client {
        val awsCreds = BasicAWSCredentials(accessKey, secretKey)
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(AWSStaticCredentialsProvider(awsCreds))
            .build() as AmazonS3Client
    }
}
