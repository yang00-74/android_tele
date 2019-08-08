实现需求的patch主要涉及到两个git仓库的修改.
1.vendor/sprd/platform/frameworks/base/packages/SystemUI
  对应的patch文件为vendor.patch
  主要修改的文件为:
  vendor/sprd/platform/frameworks/base/packages/SystemUI/res/values/config.xml
  修改点是配置新的下拉栏数据开关

2.frameworks/base/packages/SystemUI
  对应的patch文件为frameworks.patch
  主要修改的文件有两个:
  a.frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/policy/MobileSignalController.java
    此文件的修改是将下拉栏快捷图标的信号格等级与状态栏信号等级同步显示
  b.frameworks/base/packages/SystemUI/src/com/android/systemui/qs/tiles/CellularTile.java
    此文件的修改是将快捷图标下的文字显示为运营商名称
