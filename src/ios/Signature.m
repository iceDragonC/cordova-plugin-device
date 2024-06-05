/********* Signature.m Cordova Plugin Implementation *******/

#import <Cordova/CDV.h>
#import <EsignSDK/EsignSDK.h>

#define REALNAME_CALLBACK @"esign://demo/realBack" // demo 实名 或 意愿
#define SIGN_CALLBACK @"esign://demo/signBack"     //demo 签署

@interface Signature : CDVPlugin <EsignProtocol, UINavigationControllerDelegate>

- (void)startH5Activity:(CDVInvokedUrlCommand *)command;
@property (nonatomic, strong) UIViewController *sViewConroller;
@property (nonatomic, strong) UINavigationController *navi;
@property (assign, nonatomic) BOOL isPush;
@property (nonatomic, copy) NSString* callbackId;

@property (nonatomic, copy) NSDictionary* result;

@end

@implementation Signature


- (void)startH5Activity:(CDVInvokedUrlCommand *)command {
    NSMutableDictionary *message = [command argumentAtIndex:0];
    NSString *url = message[@"url"];
    //
    [self.viewController addChildViewController:self.navi];
    [self.viewController.view addSubview:self.navi.view];
    [self.navi.view setFrame:[UIScreen mainScreen].bounds];
    [self.navi didMoveToParentViewController:self.viewController];
    
    self.callbackId = command.callbackId;
//    设置默认的结果
    self.result= @{@"key":@"cancel"};
    
    [EsignSDK esignCtrlWithUrl:url ctrl:self.sViewConroller esignProtocol:self];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.25 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        self.isPush = YES;
        self.navi.delegate = self;
    });
}
- (void)pluginInitialize {
    NSLog(@"plugin init");
    self.sViewConroller=[[UIViewController alloc] init];
    self.sViewConroller.view.backgroundColor = [UIColor whiteColor];
    UINavigationController *navi = [[UINavigationController alloc] initWithRootViewController:self.sViewConroller];
    self.navi = navi;
}

- (void)navigationController:(UINavigationController *)navigationController willShowViewController:(UIViewController *)viewController animated:(BOOL)animated {
    
    if (self.isPush && [viewController isEqual:self.sViewConroller]) {
        NSLog(@"signature willShowViewController");
        [self.navi.view removeFromSuperview];
        [self.navi removeFromParentViewController];
        self.isPush = NO;
        CDVPluginResult *pluginResult = nil;
//        NSDictionary *result = @{@"key":@"cancel"};
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: self.result];
        [pluginResult setKeepCallback:@(true)];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    }
}
- (void)navigationController:(UINavigationController *)navigationController didShowViewController:(UIViewController *)viewController animated:(BOOL)animated {
    NSLog(@"signature didShowViewController");
}

 - (void)handleOpenURL:(NSNotification *)notification {
     [EsignSDK handleAppUrl:notification schemeArr:@[REALNAME_CALLBACK, SIGN_CALLBACK]];
 }

//- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation {
//    CDVWechat *cdvWechat = [self.viewController getCommandInstance:@"wechat"];
//    return [cdvWechat handleWechatOpenURL:url];
//    [EsignSDK handleAppUrl:url schemeArr:@[REALNAME_CALLBACK, SIGN_CALLBACK]];
//}


#pragma mark - EsignProtocol

- (void)businessNode:(id)pramas {
    NSLog(@"获取到了回调:%@", pramas);
//    CDVPluginResult *pluginResult = nil;
//    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:pramas];
    
    
    
    NSString *lastStep = [self.result valueForKey:@"key"];
    
    if([lastStep isEqual:@"sign"]){
        NSLog(@"当前已经签名完成，不需要在此回调:%@", pramas);
        return;
    }
    
    self.result = pramas;
    
    NSString *step = [pramas valueForKey:@"key"];
    if([step isEqual:@"sign"]) {
        NSLog(@"签名完成闭环:%@", pramas);
        if(!self.isPush){
            NSLog(@"回调结果，但是页面已经返回:%@", pramas);
            CDVPluginResult *pluginResult = nil;
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:pramas];
    
            [pluginResult setKeepCallback:@(true)];
    
            [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
        }
    }
    
    
    


//    [pluginResult setKeepCallback:@(true)];
//
//    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
    // 拿到业务节点可对原H5 功能匹配处理   ps: 签署的话会触发两次业务节点  (意愿+签署) or (实名+签署) 需注意处理
    // [self showTitleToView:[UIApplication sharedApplication].keyWindow title:[NSString stringWithFormat:@"key:%@,res:%@", pramas[@"key"], pramas[@"res"]]];
}

@end
