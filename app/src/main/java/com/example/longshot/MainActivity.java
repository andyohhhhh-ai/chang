package com.example.longshot;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.GradientDrawable;import android.net.Uri;import android.provider.MediaStore;import android.view.*;import android.widget.*;import java.io.*;import java.util.*;

public class MainActivity extends Activity {
 ArrayList<Bitmap> images=new ArrayList<>(); ArrayList<Integer> joins=new ArrayList<>(); LinearLayout list; TextView status;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xfff8f9fc);getWindow().setNavigationBarColor(-1);getWindow().getDecorView().setSystemUiVisibility(8192);home();}
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
 void safeInsets(View root){root.setOnApplyWindowInsetsListener((v,wi)->{int top,bottom;if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=wi.getInsets(WindowInsets.Type.statusBars()|WindowInsets.Type.navigationBars());top=bars.top;bottom=bars.bottom;}else{top=wi.getSystemWindowInsetTop();bottom=wi.getSystemWindowInsetBottom();}v.setPadding(dp(22),top+dp(24),dp(22),bottom+dp(18));return wi;});root.post(()->root.requestApplyInsets());}
 TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
 void home(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(22,24,22,18);root.setBackgroundColor(0xfff4f7fb);setContentView(root);safeInsets(root);
  LinearLayout head=new LinearLayout(this);head.setGravity(Gravity.CENTER_VERTICAL);ImageView logo=new ImageView(this);logo.setImageResource(com.example.longshot.R.drawable.ic_logo);head.addView(logo,new LinearLayout.LayoutParams(52,52));
  LinearLayout titleBox=new LinearLayout(this);titleBox.setOrientation(1);titleBox.setPadding(14,0,0,0);titleBox.addView(label("长图助手",25,0xff172b4d));titleBox.addView(label("截图拼接 · 清爽无重复",13,0xff728198));head.addView(titleBox);root.addView(head);
  LinearLayout intro=new LinearLayout(this);intro.setOrientation(1);intro.setPadding(18,16,18,16);GradientDrawable card=new GradientDrawable();card.setColor(Color.WHITE);card.setCornerRadius(24);intro.setBackground(card);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.topMargin=22;root.addView(intro,cp);
  intro.addView(label("一键整理零散截图",19,0xff172b4d));TextView expl=label("自动估算上下顺序，识别相邻重叠区域。生成前先预览，顺序可长按拖动调整。",14,0xff728198);expl.setPadding(0,8,0,14);intro.addView(expl);
  TextView pick=label("＋   选择截图",16,Color.WHITE);pick.setGravity(Gravity.CENTER);pick.setPadding(12,14,12,14);GradientDrawable blue=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xf2ffffff,0xeaf2f7ff});blue.setCornerRadius(dp(30));blue.setStroke(dp(1),0xbfffffff);pick.setBackground(blue);pick.setTextColor(0xff17345c);pick.setElevation(dp(5));pick.setTranslationZ(dp(2));intro.addView(pick);pick.setOnClickListener(v->{Intent i=new Intent("android.intent.action.OPEN_DOCUMENT");i.setType("image/*");i.putExtra("android.intent.extra.ALLOW_MULTIPLE",true);i.addCategory("android.intent.category.OPENABLE");startActivityForResult(i,1);});
  status=label("还没有选择图片",14,0xff526179);status.setPadding(16,14,16,14);GradientDrawable statusBg=new GradientDrawable();statusBg.setColor(0xffe9f1ff);statusBg.setCornerRadius(16);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=14;root.addView(status,new LinearLayout.LayoutParams(-1,-2));
  list=new LinearLayout(this);list.setOrientation(1);ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(list);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,0,1);lp.topMargin=14;root.addView(sc,lp);
  TextView preview=label("预览并生成长图   →",16,Color.WHITE);preview.setGravity(Gravity.CENTER);preview.setPadding(12,15,12,15);GradientDrawable green=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xf5ffffff,0xe5edfffa});green.setCornerRadius(dp(30));green.setStroke(dp(1),0xcfffffff);preview.setBackground(green);preview.setTextColor(0xff075e50);preview.setElevation(dp(6));preview.setTranslationZ(dp(2));root.addView(preview);preview.setOnClickListener(v->preview());
 }
 protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r!=1||c!=RESULT_OK||d==null)return;images.clear();try{if(d.getClipData()!=null){for(int n=0;n<d.getClipData().getItemCount();n++)add(d.getClipData().getItemAt(n).getUri());}else if(d.getData()!=null)add(d.getData());}catch(Exception e){status.setText("读取图片失败");}autoOrder();show();}
 void add(Uri u)throws Exception{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=1;images.add(BitmapFactory.decodeStream(getContentResolver().openInputStream(u),null,o));}
 int similarity(Bitmap a,Bitmap b){
  if(a==null||b==null)return 0;
  int w=48,limit=Math.min(a.getHeight(),b.getHeight())*2/5,min=24;
  if(limit<min)return 0;
  double best=Double.MAX_VALUE;int bestK=0;
  for(int k=min;k<=limit;k+=Math.max(2,limit/50)){
   double color=0,edge=0;int n=0;
   for(int yi=0;yi<20;yi++){int y=yi*(k-1)/19;
    for(int xi=0;xi<w;xi+=2){int x=xi;
     int ax=x*a.getWidth()/w,bx=x*b.getWidth()/w;
     int p=a.getPixel(ax,a.getHeight()-k+y),q=b.getPixel(bx,y);
     color+=Math.abs(Color.red(p)-Color.red(q))+Math.abs(Color.green(p)-Color.green(q))+Math.abs(Color.blue(p)-Color.blue(q));
     int ax2=Math.min(a.getWidth()-1,ax+Math.max(1,a.getWidth()/w)),bx2=Math.min(b.getWidth()-1,bx+Math.max(1,b.getWidth()/w));
     int p2=a.getPixel(ax2,a.getHeight()-k+y),q2=b.getPixel(bx2,y);
     edge+=Math.abs((Color.red(p2)-Color.red(p))-(Color.red(q2)-Color.red(q)))+Math.abs((Color.green(p2)-Color.green(p))-(Color.green(q2)-Color.green(q)))+Math.abs((Color.blue(p2)-Color.blue(p))-(Color.blue(q2)-Color.blue(q)));
     n++;
    }
   }
   double score=(color+edge*0.7)/(n*3.0)+3.0*k/limit;
   if(score<best){best=score;bestK=k;}
  }
  // Only trim when both colors and text/edge patterns agree; ambiguous pairs stay intact.
  return best<20.0?bestK:0;
 }
 void autoOrder(){if(images.size()<2)return;ArrayList<Bitmap> src=new ArrayList<>(images),out=new ArrayList<>();int start=0,bestEnd=Integer.MAX_VALUE;for(int i=0;i<src.size();i++){int incoming=0;for(int j=0;j<src.size();j++)if(i!=j)incoming+=similarity(src.get(j),src.get(i));if(incoming<bestEnd){bestEnd=incoming;start=i;}}out.add(src.remove(start));joins.clear();while(!src.isEmpty()){Bitmap tail=out.get(out.size()-1);int bi=0,bk=0;for(int i=0;i<src.size();i++){int k=similarity(tail,src.get(i));if(k>bk){bk=k;bi=i;}}out.add(src.remove(bi));joins.add(bk);}images=out;status.setText("已按相似内容估算顺序 · 长按可拖动微调");}
 void show(){list.removeAllViews();for(int i=0;i<images.size();i++){final int ix=i;LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(10,10,14,10);GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(18);row.setBackground(bg);row.setTag(i);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,160);rp.bottomMargin=10;row.setLayoutParams(rp);
  TextView num=label(String.format(java.util.Locale.getDefault(),"%02d",i+1),16,Color.WHITE);num.setGravity(Gravity.CENTER);GradientDrawable badge=new GradientDrawable();badge.setColor(0xff2563eb);badge.setCornerRadius(18);num.setBackground(badge);row.addView(num,new LinearLayout.LayoutParams(42,42));
  ImageView iv=new ImageView(this);iv.setImageBitmap(images.get(i));iv.setScaleType(ImageView.ScaleType.CENTER_CROP);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(0,140,1);ip.setMargins(12,0,8,0);row.addView(iv,ip);TextView drag=label("☷",22,0xff8392a8);row.addView(drag);
  row.setOnLongClickListener(v->{ClipData data=ClipData.newPlainText("index",""+ix);if(android.os.Build.VERSION.SDK_INT>=24)v.startDragAndDrop(data,new View.DragShadowBuilder(v),v,0);else v.startDrag(data,new View.DragShadowBuilder(v),v,0);return true;});row.setOnDragListener((v,e)->{if(e.getAction()==DragEvent.ACTION_DROP){int from=Integer.parseInt(e.getClipData().getItemAt(0).getText().toString()),to=(int)v.getTag();if(from>=0&&from<images.size()&&to>=0&&to<images.size()){Bitmap bit=images.remove(from);images.add(to,bit);show();}return true;}return true;});list.addView(row);
 }if(images.size()>0)status.setText("共 "+images.size()+" 张截图 · 长按卡片可拖动排序");}
 Bitmap compose(){if(images.isEmpty())return null;int w=images.get(0).getWidth();long total=0;ArrayList<Bitmap> scaled=new ArrayList<>();joins.clear();for(int i=0;i<images.size();i++){Bitmap b=images.get(i);Bitmap s=Bitmap.createScaledBitmap(b,w,Math.max(1,b.getHeight()*w/b.getWidth()),true);scaled.add(s);int cut=i==0?0:similarity(scaled.get(i-1),s);joins.add(cut);total+=s.getHeight()-cut;}if(total>30000)throw new RuntimeException("图片太长，请分两次拼接");Bitmap result=Bitmap.createBitmap(w,(int)total,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(result);int y=0;Paint p=new Paint(3);for(int i=0;i<scaled.size();i++){Bitmap b=scaled.get(i);int cut=joins.get(i);c.drawBitmap(b,0,y-cut,p);y+=b.getHeight()-cut;}return result;}
 void preview(){if(images.size()==0){status.setText("请先选择截图");return;}try{Bitmap b=compose();LinearLayout box=new LinearLayout(this);box.setPadding(12,12,12,12);box.setOrientation(1);TextView note=label("顺序预览 · "+images.size()+" 张截图（长图可上下滚动）",16,0xff182230);box.addView(note);ImageView iv=new ImageView(this);iv.setImageBitmap(b);iv.setAdjustViewBounds(true);ScrollView sc=new ScrollView(this);sc.addView(iv);box.addView(sc,new LinearLayout.LayoutParams(-1,0,1));new AlertDialog.Builder(this).setTitle("确认长图").setView(box).setNegativeButton("返回调整",null).setPositiveButton("确认并保存", (d,w)->save(b)).show();}catch(Exception e){status.setText("预览失败："+e.getMessage());}}
 void save(Bitmap all){try{ContentValues v=new ContentValues();v.put(MediaStore.Images.Media.DISPLAY_NAME,"长图_"+System.currentTimeMillis()+".jpg");v.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/长图助手");Uri u=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);OutputStream o=getContentResolver().openOutputStream(u);all.compress(Bitmap.CompressFormat.JPEG,95,o);o.close();status.setText("已保存到相册：Pictures/长图助手");}catch(Exception e){status.setText("保存失败："+e.getMessage());}}
}