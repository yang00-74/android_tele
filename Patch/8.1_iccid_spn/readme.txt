修改的文件路径为:
  frameworks/opt/telephony/src/java/com/android/internal/telephony/SubscriptionController.java
  
  如之前沟通,实现的方式是在 ICCID 加载完成后,通过读取numeric确定sim卡的mccmnc,然后再通过iccid的第九位数字
  确定标识位,从而将spn设置为对应值.patch中用于测试的运营商为46001和46002,贵方根据需要修改即可.
