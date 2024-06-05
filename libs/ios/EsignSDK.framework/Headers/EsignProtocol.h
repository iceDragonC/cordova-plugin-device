//
//  EsignProtocol.h
//  EsigSDK
//
//  Created by 叶鹏飞 on 2020/5/13.
//  Copyright © 2020 叶鹏飞. All rights reserved.
//

#ifndef EsignProtocol_h
#define EsignProtocol_h
@protocol EsignProtocol <NSObject>
// 业务节点回调  { business: XXX , result: XXX }
- (void)businessNode:(id)pramas;

@end

#endif /* EsignProtocol_h */
