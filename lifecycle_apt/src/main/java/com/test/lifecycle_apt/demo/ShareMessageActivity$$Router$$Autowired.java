//package com.test.lifecycle_apt.demo;
//
//import android.util.Log;
//
//import com.luojilab.component.componentlib.router.ISyringe;
//import com.luojilab.component.componentlib.service.JsonService;
//import com.luojilab.componentservice.share.bean.AuthorKt;
//
///**
// * Auto generated by AutowiredProcessor */
//public class ShareMessageActivity$$Router$$Autowired implements ISyringe {
//  private JsonService jsonService;
//
//  @Override
//  public void inject(Object target) {
//    jsonService = JsonService.Factory.getInstance().create();
//    ShareMessageActivity substitute = (ShareMessageActivity)target;
//    substitute.magazineName = substitute.getIntent().getStringExtra("bookName");
//    if (null != jsonService) {
//      substitute.author = jsonService.parseObject(substitute.getIntent().getStringExtra("author"), AuthorKt.class);
//    } else {
//      Log.e("AutowiredProcessor", "You want automatic inject the field 'author' in class 'ShareMessageActivity' , but JsonService not found in Router");
//    }
//  }
//}
