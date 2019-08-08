1、VolteTile1.java和VolteTile2.java为新增文件，分别控制卡1和卡2的volte开关
请将该文件放在如下：
frameworks/base/packages/SystemUI/src/com/android/systemui/qs/tiles/
文件夹下
2、需要修改的文件有三个，文件路径为：
vendor/sprd/platform/frameworks/base/packages/SystemUI/res/values/config.xml
frameworks/base/packages/SystemUI/res/values/strings.xml
frameworks/base/packages/SystemUI/src/com/android/systemui/qs/tileimpl/QSFactoryImpl.java
修改的逻辑请参照附件diff文件

3、VOLTE开关快捷键图标需要自己替换，本地调试使用的是数据链接开关代替
