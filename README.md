
# react-native-webview-rtc

this is customization library of [react-native-webview-android](https://github.com/lucasferreira/react-native-webview-android).

## Getting started

`$ npm install react-native-webview-rtc --save`

### Mostly automatic installation

`$ react-native link react-native-webview-rtc`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.webviewrtc.RNWebviewPackage;` to the imports at the top of the file
  - Add `new RNWebviewPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-webview-rtc'
  	project(':react-native-webview-rtc').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-webview/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-webview-rtc')
  	```

## Sample Android Manifest

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

## Usage
```javascript
const WebViewAndroid = require('react-native-webview-rtc');

var SITE_URL = 'https://react-native-webrtc.herokuapp.com';
var URL_DOMAIN = 'react-native-webrtc.herokuapp.com';

// inside react class
constructor(props){
    super(props);
    this.state = {
                url: SITE_URL,
                status: 'No Page Loaded',
                backButtonEnabled: false,
                forwardButtonEnabled: false,
                loading: true,
                messageFromWebView: null
        }
}

goBack() {
  // you can use this callback to control web view
  this.refs.webViewAndroidSample.goBack();
}

goForward() {
  this.refs.webViewAndroidSample.goForward();
}
reload () {
  this.refs.webViewAndroidSample.reload();
}

stopLoading () {
  // stops the current load
  this.refs.webViewAndroidSample.stopLoading();
}

postMessage (data) {
  // posts a message to web view
  this.refs.webViewAndroidSample.postMessage(data);
}

evaluateJavascript (data) {
  // evaluates javascript directly on the webview instance
  this.refs.webViewAndroidSample.evaluateJavascript(data);
}

injectJavaScript (script) {
  // executes JavaScript immediately in web view
  this.refs.webViewAndroidSample.injectJavaScript(script);
}

onShouldStartLoadWithRequest (event) {

  console.log('App.js -> onShouldStartLoadWithRequest () ...');
  // currently only url & navigationState are returned

  console.log('event.url = ', event.url);
  console.log('navigatinalstate = ', event.navigationState);

    if (event.url.indexOf({URL_DOMAIN} >=0 )) {
      return true;
    } else {
      return false;
    }
}

onNavigationStateChange = (event) => {
    console.log('App.js -> onNavigationStateChange () ...');
    console.log(event);
    this.setState({
      forwardButtonEnabled: event.canGoForward,
      backButtonEnabled: event.canGoBack,
      loading: event.loading,
      url: event.url,
      status: event.title
    });
}

onMessage (event) {
    console.log('App.js -> onMessage () ...');
    this.setState({
        messageFromWebView: event.message
    });
}

javascriptToInject () {
    return `
      $(document).ready(function() {
        $('a').click(function(event) {
          if ($(this).attr('href')) {
            var href = $(this).attr('href');
            window.webView.postMessage('Link tapped: ' + href);
          }
        })
      })
    `
}

render() {
  return (
    <WebViewAndroid
      ref="webViewAndroidSample"
      javaScriptEnabled={true}
      javaScriptEnabledAndroid={true}
      geolocationEnabled={false}
      builtInZoomControls={false}
      mediaPlayUserGesture={false}
      injectedJavaScript={this.javascriptToInject()}
      onShouldStartLoadWithRequest={this.onShouldStartLoadWithRequest}
      onNavigationStateChange={this.onNavigationStateChange}
      onMessage={this.onMessage}
      url={SITE_URL}
      style={styles.containerWebView}
    />
  );
}

const styles = StyleSheet.create({
  containerWebView: {
      flex: 1,
  },
});
```

or see `App.js` file in the library
