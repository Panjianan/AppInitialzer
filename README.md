# AppInitialzer
用于子模块在Application启动时执行初始化代码的库

annonation: [ ![Download](https://api.bintray.com/packages/tsubasap91/maven/annontation/images/download.svg) ](https://bintray.com/tsubasap91/maven/annontation/_latestVersion)
core: [ ![Download](https://api.bintray.com/packages/tsubasap91/maven/core/images/download.svg) ](https://bintray.com/tsubasap91/maven/core/_latestVersion)
complier: [ ![Download](https://api.bintray.com/packages/tsubasap91/maven/compiler/images/download.svg) ](https://bintray.com/tsubasap91/maven/compiler/_latestVersion)


用法：

1. 根目录下的build.gradle添加maven仓库
```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://dl.bintray.com/tsubasap91/maven' }
    }
}
```

2.具体子模块中添加依赖
```
dependencies {
    // AS 3.0以上不用complie了，用api或者implementation
    implementation "com.tsubasa.app_initializer:core:$rootProject.ext.apiVersion"
    // 如果是kotlin的用kapt，参照demo的sample_module2的build.gradle
    annotationProcessor "com.tsubasa.app_initializer:compiler:$rootProject.ext.complierVersion"
    // ....other
}
```

3.在子模块中创建类，实现IInitializer,添加@Initable注解

sample_module1
```
@Initable
public class Initializer1 implements IInitializer {
    @Override
    public void init(Context context) {
        Log.e("IInitializer", "java子模块的初始化");
    }
}
```

sample_module2,kotlin也可以喔
```
@Initable
class Initializer2 : IInitializer {
    override fun init(context: Context?) {
        Log.e("IInitializer", "kotlin子模块的初始化")
    }
}
```

![image.png](http://upload-images.jianshu.io/upload_images/1712960-857e524cf2001cdd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
