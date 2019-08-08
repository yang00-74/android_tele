实现需求的patch主要修改了两个git仓库:
1.vendor/sprd/platform/frameworks/base/packages/SystemUI
  对应的patch文件为 vendor_systemUI.patch
  主要修改的文件为以下:
   a.vendor/sprd/platform/frameworks/base/packages/SystemUI/res/values/strings_ex.xml
      添加字符串资源,如有多语言适配请自行添加
   b.vendor/sprd/platform/frameworks/base/packages/SystemUI/res/values/config.xml
      添加快捷图标定义和是否显示数据切换图标的开关
   c.vendor/sprd/platform/frameworks/base/packages/SystemUI/src/com/android/systemui/qs/tiles/DataSwitchTile.java
      该文件为新增文件,文件中使用setCurrentSimIconByDataPhoneId 方法设置数据卡图标.当前是使用数据图标作为卡1图标,
    飞行模式图标作为卡2图标,请自行替换成贵方自己的图标文件.

2.frameworks/base/packages/SystemUI
   对应的patch文件为frameworks_systemUI.patch文件
   主要修改的文件为以下:
   a.frameworks/base/packages/SystemUI/src/com/android/systemui/qs/tileimpl/QSFactoryImpl.java
     读取开关值,创建数据切换快捷栏图标
   b.frameworks/base/packages/SystemUI/AndroidManifest.xml
      添加切换默认数据卡的权限,此处贵方如果跑 CTS测试的话,可能会有fail的风险.
      如果要修改只需要在frameworks/base/data/etc/privapp-permissions-platform.xml 文件中找到com.android.systemui,仿照其
   已有的内容,添加在frameworks/base/packages/SystemUI/AndroidManifest.xml添加的权限即可.

