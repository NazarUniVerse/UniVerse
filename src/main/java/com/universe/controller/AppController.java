package com.universe.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.universe.model.ChatGroup;
import com.universe.model.ChatMessage;
import com.universe.model.Comment;
import com.universe.model.Event;
import com.universe.model.LectureNote;
import com.universe.model.Message;
import com.universe.model.Notification;
import com.universe.model.Post;
import com.universe.model.Product;
import com.universe.model.Story;
import com.universe.model.User;
import com.universe.repository.ChatGroupRepository;
import com.universe.repository.ChatMessageRepository;
import com.universe.repository.CommentRepository;
import com.universe.repository.EventRepository;
import com.universe.repository.MessageRepository;
import com.universe.repository.NoteRepository;
import com.universe.repository.NotificationRepository;
import com.universe.repository.PostRepository;
import com.universe.repository.ProductRepository;
import com.universe.repository.StoryRepository;
import com.universe.repository.UserRepository;
import com.universe.util.FileUploadUtil;

import jakarta.servlet.http.HttpSession;

@Controller
public class AppController {

    @Autowired private UserRepository userRepo;
    @Autowired private PostRepository postRepo;
    @Autowired private CommentRepository commentRepo;
    @Autowired private MessageRepository messageRepo;
    @Autowired private NotificationRepository notificationRepo;
    @Autowired private NoteRepository noteRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private StoryRepository storyRepo;
    @Autowired private ProductRepository productRepo;
    @Autowired private ChatGroupRepository chatGroupRepo;
    @Autowired private ChatMessageRepository chatMessageRepo;

    // --- YARDIMCI METODLAR ---
    private void createNotification(User recipient, User sender, String type, String message, Post post, Long relatedGroupId) {
        if (recipient.getId().equals(sender.getId())) return; 
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setSender(sender);
        n.setType(type);
        n.setMessage(message);
        n.setPost(post);
        n.setRelatedGroupId(relatedGroupId);
        notificationRepo.save(n);
    }

    private void updateUserSession(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            user = userRepo.findById(user.getId()).orElse(user);
            user.setLastSeen(LocalDateTime.now());
            userRepo.save(user);
            session.setAttribute("user", user);
        }
    }

    // --- SOHBET SİSTEMİ (CHAT) ---
    @GetMapping("/chat")
    public String showChat(HttpSession session, Model model, @RequestParam(required = false) Long groupId) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        updateUserSession(session);
        model.addAttribute("user", user);
        model.addAttribute("unreadCount", notificationRepo.countByRecipientAndIsReadFalse(user));

        List<ChatGroup> myChats = chatGroupRepo.findByMembersContaining(user);
        model.addAttribute("chats", myChats);

        if (groupId != null) {
            ChatGroup activeChat = chatGroupRepo.findById(groupId).orElse(null);
            if (activeChat != null && activeChat.getMembers().contains(user)) {
                
                List<ChatMessage> messages = activeChat.getMessages();
                for (ChatMessage msg : messages) {
                    if (!msg.getSender().getId().equals(user.getId()) && !"Okundu".equals(msg.getStatus())) {
                        msg.setStatus("Okundu");
                        chatMessageRepo.save(msg);
                    }
                }
                
                model.addAttribute("activeChat", activeChat);
                
                if (activeChat.isPrivate()) {
                    User otherUser = activeChat.getOtherUser(user);
                    model.addAttribute("chatTitle", otherUser.getFullname());
                    model.addAttribute("chatAvatar", otherUser.getAvatarPath());
                    model.addAttribute("chatUserStatus", otherUser.getLastSeenText()); 
                    model.addAttribute("chatUserId", otherUser.getId());
                    model.addAttribute("isBlocked", user.getBlockedUsers().contains(otherUser));
                } else {
                    model.addAttribute("chatTitle", activeChat.getName());
                    model.addAttribute("chatAvatar", "/images/default-group.png");
                    model.addAttribute("chatUserStatus", activeChat.getMembers().size() + " üye");
                }
            }
        }
        
        model.addAttribute("following", user.getFollowing());
        return "chat";
    }

    @PostMapping("/chat/send")
    public String sendMessage(@RequestParam Long groupId, @RequestParam String content, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        ChatGroup group = chatGroupRepo.findById(groupId).orElse(null);
        if (group != null && group.getMembers().contains(user) && !content.trim().isEmpty()) {
            
            if(group.isPrivate()) {
                User other = group.getOtherUser(user);
                if(other.getBlockedUsers().contains(user)) { 
                    return "redirect:/chat?groupId=" + groupId + "&error=blocked";
                }
            }

            ChatMessage msg = new ChatMessage();
            msg.setContent(content);
            msg.setSender(user);
            msg.setGroup(group);
            msg.setStatus("İletildi");
            chatMessageRepo.save(msg);

            for (User member : group.getMembers()) {
                if (!member.getId().equals(user.getId())) {
                    String notifMsg = ": " + (content.length() > 20 ? content.substring(0, 20) + "..." : content);
                    createNotification(member, user, "CHAT", notifMsg, null, group.getId());
                }
            }
        }
        return "redirect:/chat?groupId=" + groupId;
    }

    @GetMapping("/chat/clear/{groupId}")
    public String clearChat(@PathVariable Long groupId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        ChatGroup group = chatGroupRepo.findById(groupId).orElse(null);
        if (group != null && group.getMembers().contains(user)) {
            chatMessageRepo.deleteAll(group.getMessages());
        }
        return "redirect:/chat?groupId=" + groupId;
    }

    @GetMapping("/user/block/{userId}")
    public String blockUser(@PathVariable Long userId, HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) return "redirect:/login";
        me = userRepo.findById(me.getId()).orElse(me);
        
        User target = userRepo.findById(userId).orElse(null);
        if (target != null && !me.getId().equals(target.getId())) {
            if (me.getBlockedUsers().contains(target)) {
                me.getBlockedUsers().remove(target); 
            } else {
                me.getBlockedUsers().add(target); 
                me.getFollowing().remove(target); 
                target.getFollowing().remove(me); 
            }
            userRepo.save(me);
            userRepo.save(target);
        }
        return "redirect:/home"; 
    }

    @GetMapping("/chat/dm/{userId}")
    public String startDm(@PathVariable Long userId, HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) return "redirect:/login";
        me = userRepo.findById(me.getId()).orElse(me);

        User other = userRepo.findById(userId).orElse(null);
        if (other == null || me.getId().equals(other.getId())) return "redirect:/home";

        ChatGroup existingChat = chatGroupRepo.findPrivateChat(me, other);
        if (existingChat != null) return "redirect:/chat?groupId=" + existingChat.getId();

        ChatGroup newChat = new ChatGroup();
        newChat.setPrivate(true);
        newChat.getMembers().add(me);
        newChat.getMembers().add(other);
        newChat.setName(other.getFullname());
        chatGroupRepo.save(newChat);

        return "redirect:/chat?groupId=" + newChat.getId();
    }

    @PostMapping("/chat/create-group")
    public String createGroup(@RequestParam String groupName, @RequestParam(required = false) List<Long> participants, HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) return "redirect:/login";
        
        if (participants != null && !participants.isEmpty()) {
            ChatGroup group = new ChatGroup();
            group.setName(groupName);
            group.setPrivate(false);
            group.getMembers().add(me);
            ChatGroup savedGroup = chatGroupRepo.save(group);
            
            for (Long uid : participants) {
                User u = userRepo.findById(uid).orElse(null);
                if (u != null) {
                    savedGroup.getMembers().add(u);
                    createNotification(u, me, "GROUP_ADD", "seni '" + groupName + "' grubuna ekledi.", null, savedGroup.getId());
                }
            }
            chatGroupRepo.save(savedGroup);
            return "redirect:/chat?groupId=" + savedGroup.getId();
        }
        return "redirect:/chat";
    }

    // --- HOME ---
    @GetMapping("/home")
    public String home(HttpSession session, Model model, @RequestParam(required = false) String keyword, @RequestParam(required = false) String category) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        updateUserSession(session);

        long unreadCount = notificationRepo.countByRecipientAndIsReadFalse(user);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);

        List<Story> stories = storyRepo.findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime.now().minusHours(24));
        model.addAttribute("stories", stories);

        List<Post> posts;
        if (keyword != null) {
            posts = postRepo.search(keyword);
            model.addAttribute("searchMode", true);
        } else if (category != null) {
            posts = postRepo.findByCategoryOrderByCreatedAtDesc(category);
            model.addAttribute("activeCategory", category);
        } else {
            posts = postRepo.findAllByOrderByCreatedAtDesc();
        }
        
        PrettyTime p = new PrettyTime(new Locale("tr"));
        for(Post post : posts) {
            Date date = Date.from(post.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
            post.setTimeAgo(p.format(date));
            if (post.getLikes().contains(user)) post.setLikedByCurrentUser(true);
            User author = userRepo.findByFullname(post.getAuthorName());
            if (author != null) { post.setAuthorId(author.getId()); post.setAuthorAvatar(author.getAvatarPath()); } 
            else { post.setAuthorAvatar("/images/default-user.png"); }
        }
        model.addAttribute("posts", posts);
        if(category != null) model.addAttribute("activeCategory", category);
        return "home";
    }

    @PostMapping("/story/upload") public String uploadStory(@RequestParam("image") MultipartFile multipartFile, HttpSession session) throws IOException { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; if (!multipartFile.isEmpty()) { Story story = new Story(); story.setUser(user); String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()); story.setImageUrl(fileName); Story savedStory = storyRepo.save(story); String uploadDir = "uploads/stories/" + savedStory.getId(); FileUploadUtil.saveFile(uploadDir, fileName, multipartFile); } return "redirect:/home"; }
    @GetMapping("/story/view/{id}") @ResponseBody public String viewStory(@PathVariable Long id, HttpSession session) { User user = (User) session.getAttribute("user"); if (user == null) return "error"; user = userRepo.findById(user.getId()).orElse(user); Story story = storyRepo.findById(id).orElse(null); if (story != null && !story.getUser().getId().equals(user.getId())) { User dbUser = userRepo.findById(user.getId()).get(); if (!story.getViewers().contains(dbUser)) { story.getViewers().add(dbUser); storyRepo.save(story); } } return "ok"; }
    @GetMapping("/story/like/{id}") public String likeStory(@PathVariable Long id, HttpSession session) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; user = userRepo.findById(user.getId()).orElse(user); Story story = storyRepo.findById(id).orElse(null); if (story != null) { User dbUser = userRepo.findById(user.getId()).get(); if (story.getLikes().contains(dbUser)) story.getLikes().remove(dbUser); else story.getLikes().add(dbUser); storyRepo.save(story); } return "redirect:/home"; }
    @PostMapping("/post") public String createPost(@RequestParam(value = "content", required = false) String content, @RequestParam("image") MultipartFile multipartFile, @RequestParam(value = "category", defaultValue = "Genel") String category, @RequestParam(value = "isAnonymous", required = false) boolean isAnonymous, HttpSession session) throws IOException { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; if ((content == null || content.trim().isEmpty()) && multipartFile.isEmpty()) return "redirect:/home"; Post post = new Post(); post.setContent(content); post.setAuthorName(user.getFullname()); post.setCategory(category); post.setAnonymous(isAnonymous); if (!multipartFile.isEmpty()) { String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()); post.setImageUrl(fileName); Post savedPost = postRepo.save(post); FileUploadUtil.saveFile("uploads/" + savedPost.getId(), fileName, multipartFile); } else { postRepo.save(post); } return "redirect:/home"; }
    
    // --- UNI-PAZAR İŞLEMLERİ (DÜZELTİLDİ) ---
    @GetMapping("/market") 
    public String showMarket(HttpSession session, Model model, @RequestParam(required = false) String category) { 
        User user = (User) session.getAttribute("user"); 
        if (user == null) return "redirect:/login"; 
        updateUserSession(session); 
        model.addAttribute("user", user); 
        model.addAttribute("unreadCount", notificationRepo.countByRecipientAndIsReadFalse(user)); 
        
        List<Product> products; 
        if (category != null && !category.isEmpty()) { 
            products = productRepo.findByCategoryOrderByCreatedAtDesc(category); 
            model.addAttribute("activeCategory", category); 
        } else { 
            products = productRepo.findAllByOrderByCreatedAtDesc(); 
        } 
        
        // VIP satıcıları öne çıkar (Null check ile güvenli hale getirildi)
        products.sort((p1, p2) -> { 
            boolean v1 = p1.getSeller() != null && p1.getSeller().isVip(); 
            boolean v2 = p2.getSeller() != null && p2.getSeller().isVip(); 
            if (v1 && !v2) return -1; 
            if (!v1 && v2) return 1; 
            return 0; 
        }); 
        
        model.addAttribute("products", products); 
        return "market"; 
    }
    
    // Ürün Ekleme (Contact Number ve diğerleri eklendi)
    @PostMapping("/market/create")
    public String createProduct(@ModelAttribute Product product, @RequestParam("image") MultipartFile multipartFile, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        product.setSeller(user);
        
        if (!multipartFile.isEmpty()) {
            String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
            product.setImageUrl(fileName);
            Product savedProduct = productRepo.save(product);
            String uploadDir = "uploads/products/" + savedProduct.getId();
            FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
        } else {
            productRepo.save(product);
        }
        return "redirect:/market";
    }

    // --- DİĞERLERİ AYNEN DEVAM EDİYOR ---
    @PostMapping("/profile/update") public String updateProfile(@RequestParam("fullname") String fullname, @RequestParam("bio") String bio, @RequestParam("department") String department, @RequestParam("music") MultipartFile musicFile, @RequestParam("avatar") MultipartFile multipartFile, HttpSession session) throws IOException { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; User currentUser = userRepo.findById(user.getId()).orElse(null); if (currentUser != null) { currentUser.setFullname(fullname); currentUser.setBio(bio); currentUser.setDepartment(department); if (!multipartFile.isEmpty()) { String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()); currentUser.setAvatar(fileName); String uploadDir = "uploads/users/" + currentUser.getId(); FileUploadUtil.saveFile(uploadDir, fileName, multipartFile); } if (!musicFile.isEmpty()) { String musicName = StringUtils.cleanPath(musicFile.getOriginalFilename()); currentUser.setProfileMusic(musicName); String uploadDir = "uploads/users/" + currentUser.getId(); FileUploadUtil.saveFile(uploadDir, musicName, musicFile); } userRepo.save(currentUser); session.setAttribute("user", currentUser); } return "redirect:/profile"; }
    @GetMapping("/post/save/{id}") public String savePost(@PathVariable Long id, HttpSession session) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; user = userRepo.findById(user.getId()).orElse(user); Post post = postRepo.findById(id).orElse(null); if (post != null) { if (user.getSavedPosts().contains(post)) { user.getSavedPosts().remove(post); } else { user.getSavedPosts().add(post); } userRepo.save(user); } return "redirect:/home"; }
    @GetMapping("/saved-posts") public String showSavedPosts(HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; updateUserSession(session); List<Post> savedPosts = new ArrayList<>(user.getSavedPosts()); PrettyTime p = new PrettyTime(new Locale("tr")); for(Post post : savedPosts) { Date date = Date.from(post.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()); post.setTimeAgo(p.format(date)); if (post.getLikes().contains(user)) post.setLikedByCurrentUser(true); User author = userRepo.findByFullname(post.getAuthorName()); if (author != null) { post.setAuthorId(author.getId()); post.setAuthorAvatar(author.getAvatarPath()); } else { post.setAuthorAvatar("/images/default-user.png"); } } model.addAttribute("savedPosts", savedPosts); model.addAttribute("user", user); model.addAttribute("unreadCount", notificationRepo.countByRecipientAndIsReadFalse(user)); return "saved-posts"; }
    @GetMapping("/") public String index(HttpSession session) { if (session.getAttribute("user") != null) return "redirect:/home"; return "index"; }
    @GetMapping("/register") public String showRegister() { return "register"; }
    @PostMapping("/register") public String registerUser(User user, Model model) { String email = user.getEmail(); if (email == null || (!email.endsWith(".edu.tr") && !email.endsWith(".edu"))) { model.addAttribute("error", "Üzgünüz! Sadece üniversite e-postası ile kayıt olabilirsiniz."); return "register"; } User existingUser = userRepo.findByEmail(email); if (existingUser != null) { model.addAttribute("error", "Bu e-posta adresi zaten sisteme kayıtlı!"); return "register"; } user.setVerified(false); userRepo.save(user); return "redirect:/login"; }
    @GetMapping("/login") public String showLogin() { return "login"; }
    @PostMapping("/login") public String loginUser(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) { User user = userRepo.findByEmail(email); if (user != null && user.getPassword().equals(password)) { session.setAttribute("user", user); return "redirect:/home"; } model.addAttribute("error", "Hatalı email veya şifre!"); return "login"; }
@GetMapping("/notes") 
    public String showNotes(HttpSession session, Model model, @RequestParam(required = false) String dept, @RequestParam(required = false) String type) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        updateUserSession(session);
        model.addAttribute("user", user);
        model.addAttribute("unreadCount", notificationRepo.countByRecipientAndIsReadFalse(user));
        
        List<LectureNote> notes;
        
        if (type != null && !type.isEmpty()) {
            // Tipe göre getir (Ders Notu vb.)
            notes = noteRepo.findByTypeOrderByCreatedAtDesc(type);
        } else if (dept != null && !dept.isEmpty()) {
            // Bölüme göre getir
            notes = noteRepo.findByDepartmentOrderByCreatedAtDesc(dept);
            model.addAttribute("activeDept", dept);
        } else {
            // Hepsini getir
            notes = noteRepo.findAllByOrderByCreatedAtDesc();
        }
        
        model.addAttribute("notes", notes);
        return "notes";
    }
   @PostMapping("/notes/upload") 
    public String uploadNote(
            @RequestParam("title") String title, 
            @RequestParam("description") String description, 
            @RequestParam("department") String department, 
            // YENİ: Type parametresi eklendi
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file, 
            HttpSession session) throws IOException {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        
        if (!file.isEmpty()) {
            LectureNote note = new LectureNote();
            note.setTitle(title);
            note.setDescription(description);
            note.setDepartment(department); // Varsayılan bölüm yoksa genel
            note.setType(type); // Tipi kaydet!
            note.setUploader(user);
            
            String fileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
            note.setFileName(fileName);
            
            LectureNote savedNote = noteRepo.save(note);
            String uploadDir = "uploads/notes/" + savedNote.getId();
            com.universe.util.FileUploadUtil.saveFile(uploadDir, fileName, file);
        }
        return "redirect:/notes";
    }
    @GetMapping("/notes/download/{id}") public ResponseEntity<Resource> downloadNote(@PathVariable Long id) { LectureNote note = noteRepo.findById(id).orElse(null); if (note == null) return ResponseEntity.notFound().build(); Path filePath = Paths.get("uploads/notes/" + note.getId() + "/" + note.getFileName()); Resource resource; try { resource = new UrlResource(filePath.toUri()); if(resource.exists() || resource.isReadable()) { return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + note.getFileName() + "\"").contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource); } } catch (MalformedURLException e) { e.printStackTrace(); } return ResponseEntity.notFound().build(); }
    @GetMapping("/notifications") public String showNotifications(HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; List<Notification> notifications = notificationRepo.findByRecipientOrderByCreatedAtDesc(user); PrettyTime p = new PrettyTime(new Locale("tr")); for (Notification n : notifications) { Date date = Date.from(n.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()); n.setTimeAgo(p.format(date)); if (!n.isRead()) { n.setRead(true); notificationRepo.save(n); } } model.addAttribute("notifications", notifications); model.addAttribute("user", user); return "notifications"; }
    @GetMapping("/post/like/{id}") public String likePost(@PathVariable Long id, HttpSession session) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; Post post = postRepo.findById(id).orElse(null); User currentUser = userRepo.findById(user.getId()).orElse(null); if (post != null && currentUser != null) { if (post.getLikes().contains(currentUser)) { post.getLikes().remove(currentUser); } else { post.getLikes().add(currentUser); User postOwner = userRepo.findByFullname(post.getAuthorName()); if (postOwner != null) createNotification(postOwner, currentUser, "LIKE", "gönderini beğendi.", post, null); } postRepo.save(post); } return "redirect:/home"; }
    @GetMapping("/user/{id}") public String userProfile(@PathVariable Long id, HttpSession session, Model model) { User currentUser = (User) session.getAttribute("user"); if (currentUser == null) return "redirect:/login"; currentUser = userRepo.findById(currentUser.getId()).orElse(currentUser); User visitedUser = userRepo.findById(id).orElse(null); if (visitedUser == null) return "redirect:/home"; boolean isFollowing = currentUser.getFollowing().contains(visitedUser); List<Post> userPosts = postRepo.findByAuthorNameOrderByCreatedAtDesc(visitedUser.getFullname()); PrettyTime p = new PrettyTime(new Locale("tr")); for(Post post : userPosts) { Date date = Date.from(post.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()); post.setTimeAgo(p.format(date)); if (post.getLikes().contains(currentUser)) post.setLikedByCurrentUser(true); } model.addAttribute("visitedUser", visitedUser); model.addAttribute("posts", userPosts); model.addAttribute("currentUser", currentUser); model.addAttribute("isFollowing", isFollowing); return "user-profile"; }
    @GetMapping("/user/follow/{id}") public String followUser(@PathVariable Long id, HttpSession session) { User currentUser = (User) session.getAttribute("user"); if (currentUser == null) return "redirect:/login"; currentUser = userRepo.findById(currentUser.getId()).orElse(null); User targetUser = userRepo.findById(id).orElse(null); if (currentUser != null && targetUser != null && !currentUser.equals(targetUser)) { if (currentUser.getFollowing().contains(targetUser)) { currentUser.getFollowing().remove(targetUser); } else { currentUser.getFollowing().add(targetUser); if (!currentUser.isGhostMode()) createNotification(targetUser, currentUser, "FOLLOW", "seni takip etmeye başladı.", null, null); } userRepo.save(currentUser); } return "redirect:/user/" + id; }
    @PostMapping("/post/comment") public String addComment(@RequestParam Long postId, @RequestParam String content, HttpSession session) { User user = (User) session.getAttribute("user"); Post post = postRepo.findById(postId).orElse(null); if (user != null && post != null && content != null && !content.trim().isEmpty()) { Comment comment = new Comment(); comment.setContent(content); comment.setAuthorName(user.getFullname()); comment.setPost(post); commentRepo.save(comment); User postOwner = userRepo.findByFullname(post.getAuthorName()); if (postOwner != null) createNotification(postOwner, user, "COMMENT", "gönderine yorum yaptı: " + content, post, null); } return "redirect:/home"; }
    @GetMapping("/post/delete/{id}") public String deletePost(@PathVariable Long id, HttpSession session) { User user = (User) session.getAttribute("user"); Post post = postRepo.findById(id).orElse(null); if (user != null && post != null && post.getAuthorName().equals(user.getFullname())) { postRepo.delete(post); } return "redirect:/home"; }
    @GetMapping("/profile") public String showProfile(HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; user = userRepo.findById(user.getId()).orElse(user); model.addAttribute("user", user); return "profile"; }
    @GetMapping("/admin/messages") public String viewMessages(HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null || !user.getEmail().equals("admin@universe.edu.tr")) return "redirect:/home"; List<Message> messages = messageRepo.findAll(); model.addAttribute("messages", messages); return "messages"; }
    @PostMapping("/contact") public String sendMessage(Message message) { messageRepo.save(message); return "redirect:/?messageSent"; }
    @GetMapping("/settings") public String showSettings(HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; user = userRepo.findById(user.getId()).orElse(user); model.addAttribute("user", user); return "settings"; }
    @PostMapping("/settings/change-password") public String changePassword(@RequestParam String currentPassword, @RequestParam String newPassword, HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; User currentUser = userRepo.findById(user.getId()).orElse(null); if (currentUser != null && currentUser.getPassword().equals(currentPassword)) { currentUser.setPassword(newPassword); userRepo.save(currentUser); model.addAttribute("success", "Şifreniz başarıyla değiştirildi!"); } else { model.addAttribute("error", "Mevcut şifrenizi yanlış girdiniz!"); } model.addAttribute("user", currentUser); return "settings"; }
    @PostMapping("/settings/delete-account") public String deleteAccount(@RequestParam String password, HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; User currentUser = userRepo.findById(user.getId()).orElse(null); if (currentUser != null && currentUser.getPassword().equals(password)) { userRepo.delete(currentUser); session.invalidate(); return "redirect:/?accountDeleted"; } else { model.addAttribute("error", "Hesabı silmek için şifrenizi doğru girmelisiniz!"); model.addAttribute("user", currentUser); return "settings"; } }
    @PostMapping("/settings/toggle-ghost") public String toggleGhostMode(HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; User currentUser = userRepo.findById(user.getId()).orElse(null); if (currentUser != null && currentUser.isVip()) { currentUser.setGhostMode(!currentUser.isGhostMode()); userRepo.save(currentUser); } else { return "redirect:/settings?error=VIP"; } session.setAttribute("user", currentUser); return "redirect:/settings"; }
    @GetMapping("/logout") public String logout(HttpSession session) { session.invalidate(); return "redirect:/"; }
    @GetMapping("/events") public String showEvents(HttpSession session, Model model) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; updateUserSession(session); model.addAttribute("user", user); List<Event> events = eventRepo.findByDateGreaterThanEqualOrderByDateAscTimeAsc(LocalDate.now()); events.sort((e1, e2) -> { boolean v1 = e1.getOrganizer().isVip(); boolean v2 = e2.getOrganizer().isVip(); if (v1 && !v2) return -1; if (!v1 && v2) return 1; return 0; }); for (Event e : events) { if (e.getAttendees().contains(user)) { e.setAttending(true); } } model.addAttribute("events", events); return "events"; }
    @PostMapping("/events/create") public String createEvent(@ModelAttribute Event event, @RequestParam("image") MultipartFile multipartFile, HttpSession session) throws IOException { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; event.setOrganizer(user); if (!multipartFile.isEmpty()) { String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename()); event.setImageUrl(fileName); Event savedEvent = eventRepo.save(event); String uploadDir = "uploads/events/" + savedEvent.getId(); FileUploadUtil.saveFile(uploadDir, fileName, multipartFile); } else { eventRepo.save(event); } return "redirect:/events"; }
    @GetMapping("/events/join/{id}") public String joinEvent(@PathVariable Long id, HttpSession session) { User user = (User) session.getAttribute("user"); if (user == null) return "redirect:/login"; Event event = eventRepo.findById(id).orElse(null); User currentUser = userRepo.findById(user.getId()).orElse(null); if (event != null && currentUser != null) { if (event.getAttendees().contains(currentUser)) { event.getAttendees().remove(currentUser); } else { event.getAttendees().add(currentUser); } eventRepo.save(event); } return "redirect:/events"; }
}