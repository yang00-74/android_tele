实现需求的patch主要涉及到3个git仓库.
1.vendor/sprd/platform/frameworks/opt/telephony
  对应的patch文件为 vendor_frameworks.patch
  修改的文件为:
     vendor/sprd/platform/frameworks/opt/telephony/ex-interface/com/android/internal/telephony/TeleUtils.java
     主要添加了isOperatorForbidden4G方法用于判断是否要避免使用某个运营商的4G网络.当前方法实现中已经将联通的
  3个mccmnc加入,如果贵方对其他运营商有相同需求,仿照已有代码加入其mccmnc即可.

2.frameworks/opt/telephony
  对应的patch文件为 frameworks.patch.
  修改的文件为:
      frameworks/opt/telephony/src/java/com/android/internal/telephony/SubscriptionInfoUpdater.java
      主要引用了1中添加的方法,对符合条件的运营商sim卡设置其网络偏好为 3G/2G,也就不会驻上4G网络

3.vendor/sprd/platform/packages/services/Telephony
  对应的patch文件为 vendor_packages.patch
  修改的文件为:
     vendor/sprd/platform/packages/services/Telephony/src/com/android/phone/ExtendedNetworkSettings.java
     主要修改设置中的显示,将网络偏好中的4G选项屏蔽掉,用户也就不能手动选到4G网络
  
