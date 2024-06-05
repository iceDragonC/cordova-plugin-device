//
//  EsignSDK.h
//  EsignSDK
//
//  Created by 叶鹏飞 on 2020/5/13.
//  Copyright © 2020 叶鹏飞. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <EsignSDK/EsignProtocol.h>
NS_ASSUME_NONNULL_BEGIN

// 当前framework版本，仅用于标注
#define ESIGNFACE_IOS_FRAMEWORK_VERSION 1.0.0

@interface EsignSDK : NSObject

/*!
@method 通过SDK 加载H5操作
@param  _url          需加载的H5地址
@param  _ctrl        用于跳转视图
@param  _esignProtocol  回调
*/
+ (void)esignCtrlWithUrl:(NSString *)_url ctrl:(UIViewController *)_ctrl esignProtocol:(id<EsignProtocol>)_esignProtocol;


/*!
@method App跳转回来时的操作 (主要用于支付宝刷脸会跳，建议放在处理的最前面   返回为YES时表示符合跳转规则，已进行处理)
@param  _url                      跳转时的url
@param  _schemeArr         app scheme (需要跟获取H5地址传入的scheme 相对应) 支持传入多个  满足条件的会进行解析
*/
+ (BOOL)handleAppUrl:(NSURL *)_url schemeArr:(NSArray *)_schemeArr;

@end

NS_ASSUME_NONNULL_END
