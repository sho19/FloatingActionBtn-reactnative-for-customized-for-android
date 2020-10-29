/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import {
  showFloatingBubble,
  hideFloatingBubble,
  requestPermission,
  checkPermission,
  showFloatingBubbleText,
  initialize,
} from './native/floatingBubble';
import {
  SafeAreaView,
  StyleSheet,
  ScrollView,
  View,
  Text,
  StatusBar,
  DeviceEventEmitter,
  AppState,
} from 'react-native';

const App = () => {
  // To display the bubble over other apps you need to get 'Draw Over Other Apps' permission from androind.
  // If you initialize without having the permission App could crash
  requestPermission()
    .then(() => console.log('Permission Granted'))
    .catch(() => console.log('Permission is not granted'));

  // Initialize bubble manage
  initialize().then(() => console.log('Initialized the bubble mange'));

  // Show Floating Bubble: x=10, y=10 position of the bubble

  React.useEffect(() => {
    DeviceEventEmitter.addListener('floating-bubble-press', (e) => {
      // What to do when user press the bubble
      console.log('Press Bubble');
    });
    DeviceEventEmitter.addListener('floating-bubble-remove', (e) => {
      // What to do when user removes the bubble
      console.log('Remove Bubble');
    });

    AppState.addEventListener('change', (e) => {
      if (e === 'active')
        hideFloatingBubble().then(() => console.log('Floating Bubble Removed'));
      if (e === 'background')
        showFloatingBubble(10, 10).then(() =>
          console.log('Floating Bubble Added'),
        );
    });

    return () => {
      DeviceEventEmitter.removeAllListeners();
    };
  }, []);
  return (
    <>
      <StatusBar barStyle="dark-content" />
      <SafeAreaView>
        {/* <View onPress={()=>{
          requestPermission()
        }}> */}
        <View>
          <Text>{'request permissions'}</Text>

          <Text
            onPress={() => {
              initialize();
            }}>
            {'start buble'}
          </Text>

          <Text
            onPress={() => {
              showFloatingBubble(10, 10).then(() =>
                console.log('Floating Bubble Added'),
              );
            }}>
            {'start buble'}
          </Text>

          <Text
            onPress={() => {
              // Hide Floatin Bubble
              hideFloatingBubble().then(() =>
                console.log('Floating Bubble Removed'),
              );
            }}>
            {'stop buble'}
          </Text>
          <Text
            onPress={() => {
              // Hide Floatin Bubble
              showFloatingBubbleText('i am called')
                .then(() => console.log('text displayed'))
                .catch((e) => {
                  console.log(e);
                });
            }}>
            {'show text'}
          </Text>
        </View>
      </SafeAreaView>
    </>
  );
};

const styles = StyleSheet.create({});

export default App;
