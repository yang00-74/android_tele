实现需求的主要修改的文件如下:
1.frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/policy/MobileSignalController.java
   对应的patch文件为frameworks.patch
   在该文件中,主要增加根据sim卡对应的卡槽获取对应数据类型图标和Volte图标的逻辑.展讯的设计中对vowifi只有全局
   图标,没有每张sim卡单独对应的vowifi图标,故没有做相关修改.另外,该修改只针对双VoLte机器,如果是单volte的机器
   请与我处联系再讨论是否进一步修改.
   
2.vendor/sprd/platform/frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/policy/TelephonyIconsEx.java
   对应的patch文件为vendor.patch
   在该文件中,主要是定义添加了不同卡槽对应的数据类型图标和volte图标.该patch中我处是以项目中已有的其他图标来做开发验证的,
   贵方需要自行添加自己的资源文件,然后根据变量命名将其替换为贵方资源文件即可.
