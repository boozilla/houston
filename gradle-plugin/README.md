# Houston asset server gradle plugin

It is a helper that helps achieve synchronization automation through the build script of the schema serialized in
protobuf format from the data of the excel file uploaded to ‘Houston asset server’ through this plugin.

## Configuration

First, an example using all the setting items available in this plugin.

```
houston {
    dev {
        scope 'SERVER'
        url 'dev-houston.hostname.com:443'
        tls true
        path 'src/main/proto/asset'
        token '''
            [REDACTED]
            '''
    }
    
    prod {
        scope 'CLIENT'
        url 'prod-houston.hostname.com:443'
        tls true
        path 'src/main/proto/asset'
        token '''
            [REDACTED]
            '''
    }
}
```

Below is a detailed description of each setting.

| Name  | Description                                                                                           |
|-------|-------------------------------------------------------------------------------------------------------|
| scope | The scope value of the asset you want to sync. The values `CLIENT` and `SERVER` are allowed.          |
| url   | The hostname of the Houston Asset Server you want to synchronize.                                     |
| token | This is the token with Admin privileges set in Houston Asset Server.                                  |
| path  | This is where the synced proto schema files are stored. default base path is `'src/main/proto/asset'` |
| tls   | Whether to use TLS to communicate with the server                                                     |