

<!-- TOC -->

- [1. ~~反射的实现原理~~](#1-反射的实现原理)
    - [1.1. 动态编译和静态编译](#11-动态编译和静态编译)
    - [1.2. 反射原理](#12-反射原理)
        - [1.2.1. Class.forName](#121-classforname)
        - [1.2.2. Class.getDeclaredMethods](#122-classgetdeclaredmethods)

<!-- /TOC -->



&emsp; **<font color = "red">总结：</font>**  
1. 调用反射的总体流程如下：  
  * 准备阶段：编译期装载所有的类，将每个类的元信息保存至Class类对象中，每一个类对应一个Class对象。  
  * 获取Class对象：调用x.class/x.getClass()/Class.forName() 获取x的Class对象clz（这些方法的底层都是native方法，是在JVM底层编写好的，涉及到了JVM底层，就先不进行探究了）。  
  * 进行实际反射操作：通过clz对象获取Field/Method/Constructor对象进行进一步操作。  
2. 源码流程：  
  * Class.forName 
      * 通过JNI调用到C层，再将类名转换成Descriptor
      * 通过Runtime获取ClassLinker对象
      * 通过LookupClass在boot_class_path中寻找Class，找到则返回
      * 通过BootClassLoader中寻找class，找到则返回
      * 判断当前线程是否允许回调Java层函数，如果允许则开始校验描述符规则
      * 通过VMStack.getCallingClassLoader获取当前ClassLoader，接着调用ClassLoader.loadClass返回Class
      * 更新ClassLoader的ClassTable
  * Class.getDeclaredMethods 
      * 通过Class对象找到method_的值，即为方法区的地址  
      * 通过8bit的大小来分割Method的地址  


# 1. ~~反射的实现原理~~
&emsp; Class类与java.lang.reflect库一起对反射的概念提供了技术支持。java.lang.reflect类库包含了Field类，Method类以及Constructor类。这些类用来表示未知类里对应的成员。Class类提供了获取getFields()、getMethods()和getConstructors()等方法，而这些方法的返回值类型就定义在java.lang.reflect当中。  


&emsp; 对于反射机制而言.class文件在编译时是不可获取的，所以是在运行时获取和检查.class文件。  

&emsp; 总结起来说就是，反射是通过Class类和java.lang.reflect类库一起支持而实现的，其中每一个Class类的对象都对应了一个类，这些信息在编译时期就已经被存在了.class文件里面了，Class 对象是在加载类时由 Java 虚拟机以及通过调用类加载器中的defineClass方法自动构造的。对于我们定义的每一个类，在虚拟机中都有一个应的Class对象。  

&emsp; 那么在运行时期，无论是通过字面量还是forName方法获取Class对象，都是去根据这个类的全限定名（全限定名必须是唯一的，这也间接回答了为什么类名不能重复这个问题。）然后获取对应的Class对象。  

&emsp; 总结: java虚拟机帮我们生成了类的class对象,而通过类的全限定名，我们可以去获取这个类的字节码.class文件，然后再获取这个类对应的class对象，再通过class对象提供的方法结合类Method,Filed,Constructor，就能获取到这个类的所有相关信息. 获取到这些信息之后，就可以使用Constructor创建对象，用get和set方法读取和修改与Field对象相关的字段，用invoke方法调用与Method对象关联的方法。  



-----------


## 1.1. 动态编译和静态编译  
<!--
~~
面试官：什么是Java反射？它的应用场景有哪些？ 
https://mp.weixin.qq.com/s/TqSLUWYWfhHjpfI_srETJg

-->

反射用到的是动态编译，既然有动态编译，就会有静态编译。    

编译：

动态编译和静态编译又有什么区别？  
静态编译：在编译的时候进确定类型，如果绑定对象成功，new 是静态加载类，就编译通过。  
动态编译：在运行的时候确定类型，绑定对象。最大发挥了Java的多态，降低类之间的耦合性。

---------
<!--
理解Java反射的正确姿势
https://mp.weixin.qq.com/s/_2VVj3AN-mAuguUIk9-8xg
-->

对于反射的执行过程的原理，我这里画了一张图，以供大家参考理解。  
![image](https://gitee.com/wt1814/pic-host/raw/master/images/java/JDK/java8/java-9.png)  

我们看过JVM的相关书籍都会详细的了解到，Java文件首先要通过编译器编译，编译成Class文件，然后通过类加载器(ClassLoader)将class文件加载到JVM中。  

在JVM中Class文件都与一个Class对象对应，在因为Class对象中包含着该类的类信息，只要获取到Class对象便可以操作该类对象的属性与方法。  

在这里深入理解反射之前先来深入的理解Class对象，它包含了类的相关信息。  

Java中我们在运行时识别对象和类的信息，也叫做RTTI，方式主要有来两种：传统的RTTI（Run-Time Type Information）、反射机制。  
  

&emsp; 如果不知道某个对象的确切类型(即list引用到底是ArrayList类型还是LinkedList类型)，RTTI可以告诉你，但是有一个前提：这个类型在编译时必须已知，这样才能使用RTTI来识别它。  

&emsp; 要想理解反射的原理，必须要结合类加载机。反射机制并没有什么神奇之处，当通过反射与一个未知类型的对象打交道时，JVM只是简单地检查这个对象，看它属于哪个特定的类，然后再通过拿到的某一个类的全限定名去找这个类的Class文件 。因此，那个类的.class对于JVM来说必须是可获取的，要么在本地机器上，要么从网络获取。所以对于RTTI和反射之间的真正区别只在于:  

    RTTI，编译器在编译时打开和检查.class文件
    反射，运行时打开和检查.class文件

----------


## 1.2. 反射原理  
<!--
Java反射原理
https://cloud.tencent.com/developer/article/1695077?from=information.detail.%E5%8F%8D%E5%B0%84%E5%8E%9F%E7%90%86

Java反射机制原理探究
https://zhuanlan.zhihu.com/p/162971344
-->

&emsp; 调用反射的总体流程如下：  

* 准备阶段：编译期装载所有的类，将每个类的元信息保存至Class类对象中，每一个类对应一个Class对象。  
* 获取Class对象：调用x.class/x.getClass()/Class.forName() 获取x的Class对象clz（这些方法的底层都是native方法，是在JVM底层编写好的，涉及到了JVM底层，就先不进行探究了）。  
* 进行实际反射操作：通过clz对象获取Field/Method/Constructor对象进行进一步操作。  

&emsp; 源码流程：  
* Class.forName 
    * 通过JNI调用到C层，再将类名转换成Descriptor
    * 通过Runtime获取ClassLinker对象
    * 通过LookupClass在boot_class_path中寻找Class，找到则返回
    * 通过BootClassLoader中寻找class，找到则返回
    * 判断当前线程是否允许回调Java层函数，如果允许则开始校验描述符规则
    * 通过VMStack.getCallingClassLoader获取当前ClassLoader，接着调用ClassLoader.loadClass返回Class
    * 更新ClassLoader的ClassTable
* Class.getDeclaredMethods 
    * 通过Class对象找到method_的值，即为方法区的地址  
    * 通过8bit的大小来分割Method的地址  

### 1.2.1. Class.forName
1. 在Java层通过Class.forName来查找对应的Class  
&emsp; 如果传入的classLoader为空 , 则会通过VMStack.getCallingClassLoader获取ClassLoader  

```java
public static Class<?> forName(String name, boolean initialize,ClassLoader loader)
        throws ClassNotFoundException
    {
        if (loader == null) {
            // 如果从VMStack中获取ClassLoader失败 , 则从BootClassLoader中查找
            loader = BootClassLoader.getInstance();
        }
        Class<?> result;
        try {
            // 调用JNI层方法
            result = classForName(name, initialize, loader);
        } catch (ClassNotFoundException e) {
            Throwable cause = e.getCause();
            if (cause instanceof LinkageError) {
                throw (LinkageError) cause;
            }
            throw e;
        }
        return result;
    }
```

&emsp; 在/art/runtime/native/java_lang_class.cc文件中 , 通过Class.forName方法来获取对应的Class对象  
&emsp; 将包名换成C层的描述符 : Lcom/test/test   
&emsp; 在C层获取ClassLoader的Handle  
&emsp; 通过ClassLinker->FindClass找到Class指针  

```java
static jclass Class_classForName(JNIEnv* env, jclass, jstring javaName, jboolean initialize,  jobject javaLoader) {
  // 在ScopedFastNativeObjectAccess中 , 保存了JNIEnv对象以及所在的Thread对象
  ScopedFastNativeObjectAccess soa(env);
  ScopedUtfChars name(env, javaName);
  // 判断字符串是否为空
  if (name.c_str() == nullptr) {
    return nullptr;
  }
  // 将com.test.test转换成com/test/test验证二进制className
  if (!IsValidBinaryClassName(name.c_str())) {
    soa.Self()->ThrowNewExceptionF("Ljava/lang/ClassNotFoundException;",
                                   "Invalid name: %s", name.c_str());
    return nullptr;
  }
  // 将com.test.test转换成Lcom/test/test , 生成描述符
  std::string descriptor(DotToDescriptor(name.c_str()));
  // 从soa.Self中获取JNIEnv所在的线程对象
  StackHandleScope<2> hs(soa.Self());
  // 获取javaLoader的指针
  Handle<mirror::ClassLoader> class_loader(
      hs.NewHandle(soa.Decode<mirror::ClassLoader>(javaLoader)));
  // 从Rumtime.Current中获取ClassLinker对象
  ClassLinker* class_linker = Runtime::Current()->GetClassLinker();
  // 通过ClassLinker.findClass找到Class对象 , 并且创建相关Handle
  Handle<mirror::Class> c(
      hs.NewHandle(class_linker->FindClass(soa.Self(), descriptor.c_str(), class_loader)));
  // 如果从class_table与ClassLoader中加载/获取失败的话
  if (c == nullptr) {
    ScopedLocalRef<jthrowable> cause(env, env->ExceptionOccurred());
    env->ExceptionClear();
    jthrowable cnfe = reinterpret_cast<jthrowable>(
        env->NewObject(WellKnownClasses::java_lang_ClassNotFoundException,
                       WellKnownClasses::java_lang_ClassNotFoundException_init,
                       javaName,
                       cause.get()));
    if (cnfe != nullptr) {
      // 抛出异常
      env->Throw(cnfe);
    }
    return nullptr;
  }
  if (initialize) {
    // 如果class不为空 , 并且已经初始化完 , 则会再确认初始化
    class_linker->EnsureInitialized(soa.Self(), c, true, true);
  }
  // 返回class对象的引用
  return soa.AddLocalReference<jclass>(c.Get());
}
```

&emsp; 在ClassLinker.FindClass中开始寻找Class指针  
&emsp; 通过LookupClass找Class指针  
&emsp; 如果Class加载失败 , 并且传入的classloader为空 , 则通过boot_class_path加载  
&emsp; 如果从boot_class_path找到了Class , 则会通过DefineClass加载class并且返回  
&emsp; 开始从BootClassloader中寻找class  
&emsp; 如果没找到 , 则判断当前线程是否允许回调Java层函数 , 失败则抛出异常  
&emsp; 如果允许回调Java层函数 , 则开始校验描述符规则  
&emsp; 通过描述符规则校验后 , 调用classLoader.loadClass返回class指针  
&emsp; 找到class后 , 会将ClassLoader的ClassTable更新  
&emsp; 最后返回class指针  

```java
mirror::Class* ClassLinker::FindClass(Thread* self,
                                      const char* descriptor,
                                      Handle<mirror::ClassLoader> class_loader) {
 // 计算描述符的hash值 , 即Lcom/test/test的hash值
  const size_t hash = ComputeModifiedUtf8Hash(descriptor);
  // 在ClassLinker的ClassTable中找Class对象
  ObjPtr<mirror::Class> klass = LookupClass(self, descriptor, hash, class_loader.Get());
  if (klass != nullptr) {
    return EnsureResolved(self, descriptor, klass);
  }
  // 如果Class还没有加载的话 , 并且传入的ClassLoader没有创建的话
  if (descriptor[0] != '[' && class_loader == nullptr) {
    // 开在boot class path中寻找class对象
    ClassPathEntry pair = FindInClassPath(descriptor, hash, boot_class_path_);
    if (pair.second != nullptr) {
       // 找到的话 , 则通过DefineClass返回 class对象
      return DefineClass(self,descriptor, hash, ScopedNullHandle<mirror::ClassLoader>(),
                         *pair.first, *pair.second);
    } else {
      // 如果BootClassPath没有找到的话 , 就抛除NoClassDefFoundError
      ObjPtr<mirror::Throwable> pre_allocated =
          Runtime::Current()->GetPreAllocatedNoClassDefFoundError();
      self->SetException(pre_allocated);
      return nullptr;
    }
  }
  // 返回结果对应的Class的指针
  ObjPtr<mirror::Class> result_ptr;
  bool descriptor_equals;
  // 如果描述符是个数组的话
  if (descriptor[0] == '[') {
    // 通过classLoader创建Array Class
    result_ptr = CreateArrayClass(self, descriptor, hash, class_loader);
    descriptor_equals = true;
  } else {
    ScopedObjectAccessUnchecked soa(self);
    // 开始在BaseDexClassLoader中查找Class
    bool known_hierarchy =
        FindClassInBaseDexClassLoader(soa, self, descriptor, hash, class_loader, &result_ptr);
    if (result_ptr != nullptr) {
     // 如果在BaseDexClassLoader中找到Class
      descriptor_equals = true;
    } else {
      // 如果当前线程不允许从JNI回调到JAVA层到话
      if (!self->CanCallIntoJava()) {
        // 抛出NolassDefFoundError
        ObjPtr<mirror::Throwable> pre_allocated =
            Runtime::Current()->GetPreAllocatedNoClassDefFoundError();
        self->SetException(pre_allocated);
        return nullptr;
      }
      // 获取描述符的长度
      size_t descriptor_length = strlen(descriptor);
      // 开始校验描述符是否正确
      if (UNLIKELY(descriptor[0] != 'L') ||
          UNLIKELY(descriptor[descriptor_length - 1] != ';') ||
          UNLIKELY(memchr(descriptor + 1, '.', descriptor_length - 2) != nullptr)) {
         // 如果校验错误 , 则返回NolassDefFoundError错误
        ThrowNoClassDefFoundError("Invalid descriptor: %s.", descriptor);
        return nullptr;
      }
      // 截取Class名字
      std::string class_name_string(descriptor + 1, descriptor_length - 2);
      // 把com/test/test替换成com.test.test包名
      std::replace(class_name_string.begin(), class_name_string.end(), '/', '.');
      // 获取ClassLoader在本线程内的引用
      ScopedLocalRef<jobject> class_loader_object(
          soa.Env(), soa.AddLocalReference<jobject>(class_loader.Get()));
      ScopedLocalRef<jobject> result(soa.Env(), nullptr);
      {
        ScopedThreadStateChange tsc(self, kNative);
        //  获取class_name
        ScopedLocalRef<jobject> class_name_object(
            soa.Env(), soa.Env()->NewStringUTF(class_name_string.c_str()));
        // 调用JNIEnv->CallObjectMethod方法 , 也就是调用ClassLoader.loadClass(className)
        result.reset(soa.Env()->CallObjectMethod(class_loader_object.get(),
                                                 WellKnownClasses::java_lang_ClassLoader_loadClass,
                                                 class_name_object.get()));
      }
      if (result.get() == nullptr && !self->IsExceptionPending()) {
         // 如果ClassLoader.loadClass失败的话 , 则打印日志并且返回
        ThrowNullPointerException(StringPrintf("ClassLoader.loadClass returned null for %s",
                                               class_name_string.c_str()).c_str());
        return nullptr;
      }
      // 如果result.get不为空 , 则获取到class的指针
      result_ptr = soa.Decode<mirror::Class>(result.get());
      // 再检查一遍Descriptor
      descriptor_equals = (result_ptr != nullptr) && result_ptr->DescriptorEquals(descriptor);
    }
  }
  // 如果还有Pending的异常没处理的话 , 则会再重新找一遍class
  if (self->IsExceptionPending()) {
    // 有可能其他线程加载完成了这个class , 所以需要再查找一遍
    result_ptr = LookupClass(self, descriptor, hash, class_loader.Get());
    if (result_ptr != nullptr && !result_ptr->IsErroneous()) {
      // 如果没有问题的话 , 则会清理异常
      self->ClearException();
      // 重新检查完没有异常的话 , 就会返回class指针
      return EnsureResolved(self, descriptor, result_ptr);
    }
    return nullptr;
  }
  // 开始插入class table中
  ObjPtr<mirror::Class> old;
  {
   // 加锁
    WriterMutexLock mu(self, *Locks::classlinker_classes_lock_);
   // 将classLoader中的classTable插入
    ClassTable* const class_table = InsertClassTableForClassLoader(class_loader.Get());
    // 在class_table中查找descriptor
    old = class_table->Lookup(descriptor, hash);
    // 如果old为空 , 代表原来的class_Table中缺失没有
    if (old == nullptr) {
      // 将class的指针赋给old
      old = result_ptr;   
      if (descriptor_equals) {
        // 如果class与descriptor匹配上了 , 则将class的指针与对应的hash值插入class_Table
        class_table->InsertWithHash(result_ptr.Ptr(), hash);
        Runtime::Current()->GetHeap()->WriteBarrierEveryFieldOf(class_loader.Get());
      }  // else throw below, after releasing the lock.
    }
  }
  // 返回class的指针 , 即地址
  return result_ptr.Ptr();
}
```

&emsp; 在ClassLinker.LookupClass中会从 :  
&emsp; ClassLoader中获取ClassTable  
&emsp; 接着从ClassLoader中查找descriptor对应的ClassTable对象  
&emsp; 再从ClassTable中获取descriotor对应的Class的指针  

```java
mirror::Class* ClassLinker::LookupClass(Thread* self,
                                        const char* descriptor,
                                        size_t hash,
                                        ObjPtr<mirror::ClassLoader> class_loader) {
  // 锁住ClassLoader所在的线程
  ReaderMutexLock mu(self, *Locks::classlinker_classes_lock_);
  // 从ClassLoader中获取ClassTable
  ClassTable* const class_table = ClassTableForClassLoader(class_loader);
  if (class_table != nullptr) {
     // 从ClassTable中获取descriptor对应的Class指针
    ObjPtr<mirror::Class> result = class_table->Lookup(descriptor, hash);
    if (result != nullptr) {
       // 如果找到则返回class对应的指针
      return result.Ptr();
    }
  }
  return nullptr;
}
```

### 1.2.2. Class.getDeclaredMethods
在Class.getDeclaredMethods调用后 , 同样会调用到JNI层  

通过jobject找到Class对象的地址  
根据klass->GetDeclaredMethods会先获取到method_的基址  
根据8bit进行分割 , 通过这些指针构建成Method对象  

```java
static jobjectArray Class_getDeclaredMethodsUnchecked(JNIEnv* env, jobject javaThis,
                                                      jboolean publicOnly) {
  ScopedFastNativeObjectAccess soa(env);
  StackHandleScope<2> hs(soa.Self());
  // 解析javaThis的class地址
  Handle<mirror::Class> klass = hs.NewHandle(DecodeClass(soa, javaThis));
  size_t num_methods = 0;
  // 根据Method地址所占用的长度开始遍历Method对象
  for (auto& m : klass->GetDeclaredMethods(kRuntimePointerSize)) {
    auto modifiers = m.GetAccessFlags();
    //  计算Method数量
    if ((publicOnly == JNI_FALSE || (modifiers & kAccPublic) != 0) &&
        (modifiers & kAccConstructor) == 0) {
      ++num_methods;
    }
  }
  // 初始化Method的ArrayClass数组
  auto ret = hs.NewHandle(mirror::ObjectArray<mirror::Method>::Alloc(
      soa.Self(), mirror::Method::ArrayClass(), num_methods));
  if (ret == nullptr) {
    soa.Self()->AssertPendingOOMException();
    return nullptr;
  }
  num_methods = 0;
  // 又开始遍历一遍
  for (auto& m : klass->GetDeclaredMethods(kRuntimePointerSize)) {
    auto modifiers = m.GetAccessFlags();
      // 满足下面条件
    if ((publicOnly == JNI_FALSE || (modifiers & kAccPublic) != 0) &&
        (modifiers & kAccConstructor) == 0) {
      // 通过函数指针(即m)创建ARTMethod对象
      auto* method =
          mirror::Method::CreateFromArtMethod<kRuntimePointerSize, false>(soa.Self(), &m);
      if (method == nullptr) {
        soa.Self()->AssertPendingException();
        return nullptr;
      }
      // 添加method
      ret->SetWithoutChecks<false>(num_methods++, method);
    }
  }
  // 返回Mthod数组
  return soa.AddLocalReference<jobjectArray>(ret.Get());
}
```

根据Class对象中methods_属性的值 , 代表Method的基址  

```java
inline LengthPrefixedArray<ArtMethod>* Class::GetMethodsPtr() {
  return reinterpret_cast<LengthPrefixedArray<ArtMethod>*>(
      static_cast<uintptr_t>(GetField64(OFFSET_OF_OBJECT_MEMBER(Class, methods_))));
}
```
