 五格信号需求
 实现五格信号需求的代码修改主要涉及两个git仓库:
 1.frameworks/base
   该仓库下修改的文件有
    a)telephony/java/android/telephony/SignalStrength.java(该文件载入配置,控制信号强度与信号格数的转换)
    b)packages/SystemUI/src/com/android/systemui/statusbar/policy/AccessibilityContentDescriptions.java(该文件修改防止数组越界)
  其patch文件为 framework_five_level.patch

 2.vendor/sprd/platform/frameworks/base
   该仓库下主要修改的文件为
    a)core/res/res/values/symbols_ex.xml(此文件修改目的为:添加配置)
    b)core/res/res/values/config_ex.xml(此文件修改目的为:设置信号强度列表,并添加是否显示5格信号的开关)
    c)packages/SystemUI/src/com/android/systemui/statusbar/policy/TelephonyIconsEx.java(此文件修改目的为:添加5格信号图标至图标group中)
    d)packages/SystemUI/res/drawable/stat_sys_signal_5_fully.xml(该文件为新添加的5格信号图标,需贵方用自己的文件替换掉,以相同名称命名)
  其patch文件为vendor_five_level.patch
