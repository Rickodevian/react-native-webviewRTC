/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View, Alert, Button, Linking, WebView} from 'react-native';
import $ from 'jquery';

var WebViewAndroid = require('react-native-webview-rtc');

var SITE_URL = 'YOUR_SITE_URL';
var URL_DOMAIN = 'YOUR_DOMAIN';

const instructions = Platform.select({
  ios: 'Click this button to making video call',
  android:'Trying to reconnect. Please check your connectivity'
});

export default class App extends Component {

    constructor(props){
        super(props);
	// put your site url to state if you want your injected script apply to all web pages
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

  _onWebviewError() {
    return (
      <View style={styles.container}>
        <Text>{instructions}</Text>
      </View>
    );
  }

  _redirectToSafari() {
    let version = parseInt(Platform.Version, 10);
    if (version >= 11) {
      Linking.openURL(SITE_URL).catch(error => {
        Alert.alert('Error', `An error occured : ${error}`);
      });
    } else {
      Alert.alert('Warning', 'You must be using iOS Safari 11 to make video call');
    }
  }

  render() {
    if (Platform.OS == 'ios') {
      return (
        <View style={styles.container}>
          <Text style={styles.welcome}> {instructions}</Text>
          <Button
            onPress={this._redirectToSafari}
            title="Call"
            accessibilityLabel="Click to make video call"
          />
        </View>
      );
    } else {
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
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  containerWebView: {
      flex: 1,
  },
});
