# Hide Guise CLI AWS SDK debug messages.

The AWS SDK gets really chatty when the logger is set to `DEBUG` level. Here's an example:

```
[DEBUG] Creating an interceptor chain that will apply interceptors in the following order: [software.amazon.awssdk.services.route53.internal.Route53IdInterceptor@67b7c170]
[DEBUG] Sending Request: DefaultSdkHttpFullRequest(httpMethod=POST, protocol=https, host=route53.amazonaws.com, encodedPath=…, headers=[amz-sdk-invocation-id, Content-Length, Content-Type, User-Agent], queryParameters=[])
[DEBUG] AWS4 String to sign: …
20200218T210716Z
20200218/us-east-1/route53/aws4_request
…
[DEBUG] Received successful response: 200
[DEBUG] Interceptor 'software.amazon.awssdk.services.route53.internal.Route53IdInterceptor@67b7c170' modified the message with its modifyResponse method.
```

The messages for uploading object to S3 are overwhelming, showing each bit of content uploaded:

```
[DEBUG] AWS4 Canonical Request Hash: …
[DEBUG] http-outgoing-10 >> "       …"
[DEBUG] http-outgoing-10 >> "       …"
[DEBUG] http-outgoing-10 >> "       …"
[DEBUG] http-outgoing-10 >> "       …"
[DEBUG] http-outgoing-10 >> "       …"
[DEBUG] http-outgoing-8 << "x-amz-id-2: …"
[DEBUG] http-outgoing-8 << "x-amz-request-id: …"
[DEBUG] http-outgoing-8 << "Date: …"
[DEBUG] http-outgoing-8 << "x-amz-server-side-encryption: …"
```

That really gets in the way. It might be a good idea to disable all the AWS debug messages.

This is related to [AWS S3 credentials problem log message but S3Client.putObject()](https://stackoverflow.com/q/57206624) succeeds.
