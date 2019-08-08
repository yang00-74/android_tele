从贵方上传的资料来看,首先需要将网络类型图标移动到信号格数上方,另外还需要修改信号格数图标.
实现需求的修改如下:

1.vendor/sprd/platform/frameworks/base/packages/SystemUI/res/layout/mobile_signal_group.xml
  主要调整了信号布局文件,将网络类型挪到了信号格数左上方,贵方可自行调整图标大小,对应的patch
  文件为 8.1_dataTypeOverRat.patch
  
2.要将信号格数的图标修改成对比机的样式,需要贵方自己准备相关的图片资源.相关代码在
vendor/sprd/platform/frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/policy/TelephonyIconsEx.java
中.信号格数的图片数组为 TELEPHONY_SIGNAL_STRENGTH ,该数组包含了从 0-4 各个等级的信号图片的资源名称,贵方只要将自己的图标
资源以相同命名替换掉项目中原有资源就可以了.
