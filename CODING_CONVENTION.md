# Quy định chung
## Quy định đặt tên
### Quy tắc đặt tên trong class
* Tên class
  Tên class theo dạng UpperCamelcase, viết hoa chữ cái đầu tiên của mỗi từ
  VD: class User, ImageSprite
* Tên hàm/phương thức
  Tên hàm/phương thức theo chuẩn lowerCamelCase, từ đầu tiên viết thường, các từ tiếp sau viết hoa chữ cái đầu.
  VD: run(), runFast(), getBackground()
* Tên biến
  Tương tự tên hàm
  VD: userName
* Tên hằng
  Viết hoa tất cả các kí tự, cách nhau bằng dấu "_"
  VD: MAX_SPEED, ID_AD_INTERSTITAL
### Quy tắc đặt tên resource
####  Basic principle
Tất cả tên resoures đặt theo convention sau
** WHAT_WHERE_DESCRIPTION_SIZE**
* WHAT
  Chỉ ra nguồn resouce này đại diện cho cái gì, thường là tên của View Class để giới hạn sự lựa chọn của resoure này.
  VD : MainActivity → activity , BaseFragment → fragment
* WHERE
  Mô tả nơi resouce này nằm ở đâu trong app. Các resourec sử dụng ở nhiều màn thường đặt là all, còn các phần cụ thể khác sẽ phải chỉ cụ thể vị trị của phần đó
  VD : MainActivity → main, MovieFragment → movie
* DESCRIPTION
  Mô tả thuộc tính của resouce trên màn hình
  VD : title, sub_title
* SIZE (optional)
  Chỉ rõ kích thước của resoure này, thường dành cho và dimensions
  VD : 24dp, small, big

![enter image description here](https://images.viblo.asia/3c93cf4c-2b44-4634-8e3f-fafb38133c51.png)


####  Layouts

![enter image description here](https://jeroenmols.com/img/blog/resourcenaming/layouts.png)
| Prefix | Usage |
|--|--|
|activity| contentview for activity|
|fragment| view for a fragment|
|view| inflated by a custom view|
|item|  layout used in list/recycler/gridview|
|layout| layout reused using the include tag|
VD:
-   **activity_main**: content view of the MainActivity
-   **fragment_articledetail**: view for the ArticleDetailFragment
-   **view_menu**: layout inflated by custom view class MenuView
-   **item_article**: list item in ArticleRecyclerView
-   **layout_actionbar_backbutton**: layout for an actionbar with a backbutton (too simple to be a customview)
#### Strings
Nếu strings này sử dụng ở màn cụ thể và duy nhất, ta chỉ định <Where> cho string đó ![](https://images.viblo.asia/ec948b3a-c553-40d4-a5f6-85fd036fa31a.png)
hoặc nếu strings đc sử dụng toàn app thì dùng ![](https://images.viblo.asia/b66277cf-5143-4e4a-939a-24c80c646559.png)
VD : article_title, error_message, all_ok, all_close
#### Drawables
Tượng tự như Strings và ta nên cân nhắc thêm lựa chọn <Size>  
** WHAT_WHERE_DESCRIPTION_SIZE**
VD: ic_all_back, shape_movie_placeholder, ic_all_default_avatar_36dp

IDs Đối với Ids của view ta có rule sau
![](https://images.viblo.asia/baa2019f-62c2-4efe-a938-757d1e79f519.png)
Trong đó <What> là tên View class, <Where> là vị trí của view

VD: tablayout_main, image_menu_profile, text_movie_title

#### Dimensions

Một app android nên xác định một vài dimension cố định và có thể sử dụng lại. Thường ta hay sử dụng all cho <WHERE>  
![](https://images.viblo.asia/384f86ca-d87b-4da1-8c97-1c36984787e2.png)
hoặc có thể tùy chọn cho size cụ thể  
![](https://images.viblo.asia/8164d02c-4a41-439a-8260-72989eab781c.png)

Trong đó <What> gồm có : width, height, size (nếu width == height) , margin, padding, elevation, keyline, textsize….

Ngoài ra còn nhiều dimension ít phổ biến hơn như animation rotate, scale …

VD : height_toolbar, size_menu_icon, textsize_big…
#### ID Resouce
Dùng theo dạng lowerCamelCase
VD:
-   **txtNameUser**: TextView
-   **edtLastName**: EditText
-  **imgAvata**: ImageView

## Comment
### Khi nào:
- Những đoạn code cần phải giải thích tại sao và chúng thật sự khó hiểu.
- Comment cho trường hợp dễ bị hiểu sai.
- Comment miêu tả cho những cái nhìn bao quát
### Như thế nào
- Comment phải mô tả được suy nghĩ của người viết code và giải đáp được những thắc mắc của người đọc.
- Không comment những đoạn code mà ý nghĩa của nó đã quá rõ ràng.

>  ### Code tells you HOW, Comments tell you WHY

## Quy định config Jenkins
### Android-Ads-Config
  * Thêm file ads config cho từng app
  * Các sản phẩm cũ sử dụng key riêng thì phải thêm file key. Những sản phẩm mới thì sử dụng key của base với config
  ``` 
      storePassword "trustedapp@123"
      keyAlias "release"
      keyPassword "trustedapp@123"
  ```
### Project
* Thêm file Jenkins config vào root
    - PARTNER = 'Inhouse' -> chỉ định đối tác: FireAnts  Inhouse  Bkplus  Uthus  VTD
    - ADS_CONFIG_FILE = 'fileConfigAds.adsconf' -> file ads config
    - RELEASE_KEY_PATH = 'Android-Ads-Config/key/release.jks' -> path release key file
    - BRANCH_MASTER = 'master' -> branch trigger build
    - DISCORD_WEBHOOK_URL = "link-webhook-discord" -> link webhook channel discord của project
* build.gradle (app):
    - [Setup theo require của module ads](https://github.com/AperoVN/Apero-Sample-Module-Ads)
 
## Folder Structure
 * Package-by-feature

    `com.example.myapp.service.* `
  
    `com.example.myapp.ui.*` 
    
    `com.example.myapp.ui.mainscreen`
    
    `com.example.myapp.ui.detailsscreen`
   

![](https://github.com/AperoVN/Apero-Base-MVVM-Hilt-Compose/blob/master/project_structure.jpg?raw=true)
    
