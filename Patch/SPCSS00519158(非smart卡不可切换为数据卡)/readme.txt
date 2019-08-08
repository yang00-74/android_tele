实现需求的patch主要涉及两个git仓库的修改:
 1.packages/services/Telephony
   修改的文件:src/com/android/phone/MobileNetworkSettings.java(修改 Mobile network菜单)
   对应的patch文件为Telephony.patch
 
2.packages/apps/Settings
    修改的文件为
        1)src/com/android/settings/datausage/DataUsageSummary.java(修改 Data usage菜单)
        2)src/com/android/settings/sim/SimDialogActivity.java(修改 Sim cards菜单)
    对应的patch文件为Settings.patch

以上修改的文件中都有一个重要的方法isSmartCard(),该方法负责判断sim卡是否属于Smart运营商.贵方测试的时候可修改该方法逻辑,以完成测试需求.
