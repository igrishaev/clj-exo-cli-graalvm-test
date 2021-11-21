
[Install GraalVM](https://www.graalvm.org/docs/getting-started/)

Install nagive image:

```bash
gu install native-image
```

Then:

- `make` to make an uberjar -> binary file

- export the creds:

```bash
export ACCESS_KEY=...
export SECRET_KEY=...
```

Run any get/list command:

- `./target/graaltest list-zones`

- `./target/graaltest list-dbaas-service-types`

- `./target/graaltest get-template <uuid>`

- `./target/graaltest get-dbaas-service-types`

- `./target/graaltest list-ssh-keys`

- `./target/graaltest get-ssh-key test2`
