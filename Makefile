
all: uberjar graal-build

uberjar:
	lein uberjar

graal-build:
	native-image --report-unsupported-elements-at-runtime \
	             --initialize-at-build-time \
	             --no-server \
	             -jar ./target/uberjar/graaltest-0.1.0-SNAPSHOT-standalone.jar \
			     -H:IncludeResources='.*public-api.json$$' \
                 --enable-url-protocols=https \
                 -H:Log=registerResource \
	             -H:Name=./target/graaltest
