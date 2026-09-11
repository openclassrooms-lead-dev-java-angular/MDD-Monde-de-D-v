# MDD Backend


### RSA keys

Generate an RSA key pair for JWT signing and verification:

```shell
openssl genrsa -out src/main/resources/keys/private.key 2048
```

```shell
openssl rsa -pubout -in src/main/resources/keys/private.key -out src/main/resources/keys/public.key
```

⚠️ The generated keys must never be committed to the repository.

