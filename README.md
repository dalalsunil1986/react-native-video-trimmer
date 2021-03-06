
# react-native-video-trimmer

## Getting started

`$ npm install react-native-video-trimmer --save`

### Mostly automatic installation

`$ react-native link react-native-video-trimmer`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-video-trimmer` and add `RNVideoTrimmer.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNVideoTrimmer.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
5. cd `ios`
6. create Podfile sample Podfile `https://facebook.github.io/react-native/docs/integration-with-existing-apps`
7. add `pod 'ICGVideoTrimmer', :git => 'https://github.com/nixplay/ICGVideoTrimmer.git'` to Podfile
8. run `pod install`

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNVideoTrimmerPackage;` to the imports at the top of the file
  - Add `new RNVideoTrimmerPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-video-trimmer'
  	project(':react-native-video-trimmer').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-video-trimmer/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-video-trimmer')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNVideoTrimmer.sln` in `node_modules/react-native-video-trimmer/windows/RNVideoTrimmer.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Video.Trimmer.RNVideoTrimmer;` to the usings at the top of the file
  - Add `new RNVideoTrimmerPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNVideoTrimmer from 'react-native-video-trimmer';

// TODO: What to do with the module?
RNVideoTrimmer;
```
  
