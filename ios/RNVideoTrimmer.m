#import "RNVideoTrimmer.h"
#import <React/RCTConvert.h>
#import <AssetsLibrary/AssetsLibrary.h>
#import <AVFoundation/AVFoundation.h>
#import <Photos/Photos.h>
#import <React/RCTUtils.h>
#import "VideoTrimmerViewController.h"
@import Photos;
@interface RNVideoTrimmer () <VideoTrimerViewDelegate>

@property (nonatomic, strong) RCTResponseSenderBlock callback;
@property (nonatomic, strong) NSDictionary *defaultOptions;
@property (nonatomic, retain) NSMutableDictionary *options, *response;
@property (nonatomic, strong) NSArray *customButtons;

@end


@implementation RNVideoTrimmer

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(showVideoTrimmer:(NSDictionary *)options callback:(RCTResponseSenderBlock)callback)
{
    self.callback = callback; // Save the callback so we can use it from the delegate methods
    self.options = [NSMutableDictionary dictionaryWithDictionary:options];

    NSString *assetID = @"";

    assetID = [self.options[@"uri"] substringFromIndex:@"ph://".length];
    float maxLength = 15;
    float minLength = 0;
    if(self.options[@"maxLength"]){
        maxLength = [self.options[@"maxLength"] floatValue];
    }
    if(self.options[@"minLength"]){
        minLength = [self.options[@"minLength"] floatValue];
    }
    PHFetchResult *results;
    results = [PHAsset fetchAssetsWithLocalIdentifiers:@[assetID] options:nil];
    if (results.count == 0) {
        NSString *errorText = [NSString stringWithFormat:@"Failed to fetch PHAsset with local identifier %@ with no error message.", assetID];
        self.callback(@[@{@"error": errorText}]);
    }


    NSBundle *podBundle = [NSBundle bundleForClass:[VideoTrimmerViewController class]];
    VideoTrimmerViewController *vc;
    NSBundle *bundle;
    id data = [podBundle URLForResource:@"RNVideoTrimmer" withExtension:@"bundle"];
    if(data != nil) {
        bundle = [NSBundle bundleWithURL:data];
    } else {
        bundle = podBundle;
    }
    vc = [[VideoTrimmerViewController alloc]initWithNibName:@"VideoTrimmerViewController" bundle:bundle];
    [vc setDelegate:self];
	[vc setMaxLength: maxLength];
    [vc setMinLength: minLength];
    dispatch_async(dispatch_get_main_queue(), ^{
        [vc setupAsset:assetID];
        UIViewController *root = RCTPresentedViewController();
        [root presentViewController:vc animated:YES completion:nil];
    });


}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

#pragma VideoTrimerViewDelegate
- (void)VideoTrimmerViewController:(nonnull VideoTrimmerViewController *)videoTrimmerController didChangeStartTime:(Float64)startTime endTime:(Float64)endTime{
    NSLog(@"VideoTrimmerViewController didChangeStartTime %f %f", startTime, endTime);
}
- (void)didFinishVideoTrimmerViewController:(nonnull VideoTrimmerViewController *)videoTrimmerController withStartTime:(Float64)startTime endTime:(Float64)endTime{
    NSLog(@"didFinishVideoTrimmerViewController withStartTime endTime %f %f", startTime, endTime);
    self.callback(@[@{
                        @"uri":self.options[@"uri"],
                        @"startTime":@(startTime),
                        @"endTime":@(endTime)
                        }]);
}
- (void)didFinishVideoTrimmerViewController:(nonnull VideoTrimmerViewController *)videoTrimmerController{
    NSLog(@"didFinishVideoTrimmerViewController");
    self.callback(@[@{@"error": @"user cancel"}]);
}
- (void)VideoTrimmerViewController:(nonnull VideoTrimmerViewController *)videoTrimmerController didFailedWithError:(NSError *)error{
    self.callback(@[@{@"error": error.localizedDescription}]);
}
@end
