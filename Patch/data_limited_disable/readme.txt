实现数据流量到达限制值时,灰显所有数据开关的需求patch如下,修改涉及的git仓库较多.
 1.framework/base(对应的patch文件为 framework_base.patch )
    修改的文件有:
    a.frameworks/base/telephony/java/android/telephony/TelephonyManager.java
       在该文件中修改了相关方法,并添加了一个方法,故贵方在编译时需要先使用命令 make update-api,以免报错
    b.frameworks/base/telephony/java/com/android/internal/telephony/TelephonyProperties.java
       在该文件中添加了一个属性字串
    c.frameworks/base/packages/SystemUI/src/com/android/systemui/qs/tiles/CellularTile.java
       该文件为下拉栏的数据开关,在此进行了数据流量到达限定值时屏蔽点击事件,从而使其无法操作数据连接
    注意:因为下拉栏数据开关有DataConnectionTile.java和CellularTile.java,我方不确定贵方使用的是哪一个,故在两个文件中都进行
         了相关屏蔽.
  
 2.vendor/sprd/platform/frameworks/base(对应的patch文件为 vendor_framework.patch )
     修改的文件为:
     a.vendor/sprd/platform/frameworks/base/packages/SystemUI/src/com/android/systemui/qs/tiles/DataConnectionTile.java
        该文件为下拉栏的另一个数据开关,修改与CellularTile.java相同.
 
 3.packages/service/Telephony (对应的patch文件为 packages_telephony.patch )
     修改的文件为:
      a.packages/services/Telephony/src/com/android/phone/MobileNetworkSettings.java
        该文件的修改用于灰显 设置-> 网络和互联网->移动网络 中的数据开关
 
 4.packages/apps/Settings (对应的patch文件为 packages_settings.patch )
     修改的文件为:
      a.packages/apps/Settings/src/com/android/settings/datausage/DataUsageSummary.java
        该文件的修改用于灰显 设置-> 网络和互联网->流量使用情况 中的数据开关
 
 5.vendor/sprd/platform/packages/apps/Settings(对应的patch文件为 vendor_settings.patch )
     修改的文件为:
     a.vendor/sprd/platform/packages/apps/Settings/src/com/android/settings/sim/DataPreference.java
       该文件的修改用于灰显 设置-> 网络和互联网->sim卡 中的数据开关

以上就是所有patch相关信息,另外有一个风险贵方必须考虑:
    当用户在流量使用情况中设置了流量上限,并且在流量使用达到上限时弹出的窗口上没有选择恢复数据使用,那么数据开关会默认关闭,并且
按照该需求的逻辑,此时所有数据开关都会灰显不可操作,也就是无法再打开数据了.此时因为数据开关已经关闭了,在流量使用情况界面无法进入
结算周期菜单,也就没有办法重新设置流量上限,从而导致无法恢复数据使用,直至重启手机才会有新的弹窗让用户有机会恢复数据使用.因此,我
方的一个建议是不需要将所有数据开关设置为不可操作,可以保留下拉栏的数据开关可操作.
