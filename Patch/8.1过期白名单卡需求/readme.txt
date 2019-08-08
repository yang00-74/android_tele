合入patch前,请将nv配置修改为以下内容:
BaseParam -> SIM_LOCK_CUSTOMIZE_DATA -> dummy2 = 0x7

在vendor/sprd/plugins/packages/services/Telephony/addons/SimLockSupport/src/plugin/sprd/simlock/SimLockManagerPlugin.java
文件中新添加的代码里mWhiteSimList是配置卡2白名单的列表,贵方可自行修改.
