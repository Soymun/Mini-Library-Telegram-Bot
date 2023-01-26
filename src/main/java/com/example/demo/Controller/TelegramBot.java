package com.example.demo.Controller;


import com.example.demo.Config.BotConfig;
import com.example.demo.Entity.Books;
import com.example.demo.Service.BookService;
import com.example.demo.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {


    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    final BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start", "get a welcome message"));
        listofCommands.add(new BotCommand("/mybook", "get your book"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            List<String> list = getReqest(update.getMessage().getText());
            if(update.getMessage().getText().contains("/check")){

            }
            else {
                String data = list.get(0);
                switch (data){
                    case "/start" ->{
                        try {
                            userService.saveUser(update.getMessage());
                            register(update.getMessage().getChatId());
                        }
                        catch (RuntimeException e){
                            try {
                                execute(new SendMessage(update.getMessage().getChatId().toString(), e.getMessage()));
                            } catch (TelegramApiException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                    case "/getbooks" ->{
                        String str = bookService.getBooks(list, 1);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(update.getMessage().getChatId().toString());
                        sendMessage.setText(str);
                        getBook(str, update.getMessage().getChatId(), bookService.count(list));
                    }
                }
            }
        }
        else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if(callbackData.equals("YES_BUTTON")){
                String text = "Приветствуем в нашем боте.\nЧтобы найти книги по автору, по названию, по жанру и узнать его ID.\nИспользуйте метод /getbooks с параметрами a,n,g (через пробел),\nгде a - автор, n - название книги, g - жанр.\nЧтобы получить книгу используйте метод /getbook с параметром id, где указываете id книги.\nУдачи найти книгу).";
                userService.acceptRegister(update.getCallbackQuery().getMessage().getChatId());
                executeEditMessageText(text, chatId, messageId);
            }
            else if(callbackData.equals("NO_BUTTON")){
                String text = "Sorry you can't find books(";
                executeEditMessageText(text, chatId, messageId);
            }
            else if(Character.isDigit(callbackData.charAt(0))){
                int page = Integer.parseInt(callbackData);
                String str = update.getCallbackQuery().getMessage().getText().split("\n")[0];
                List<String> list = getReqest(str);
                String text = bookService.getBooks(list, page);
                executeEditMessageTextBooksGet(text, update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getMessage().getMessageId(), page, bookService.count(list));
            }
        }
        else if(update.hasMessage() && update.getMessage().hasDocument()){
            String str = update.getMessage().getCaption();
            if (str.contains("/save")) {
                List<String> list = getReqest(str);
                if(list.size() != 4){
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(update.getMessage().getChatId().toString());
                    sendMessage.setText("Вы не указали один из параметров.\nПопробуйте снова");
                    executeMessage(sendMessage);
                    return;
                }
                Books books = bookService.saveBook(list);
                GetFile getFileRequest = new GetFile();

                getFileRequest.setFileId(update.getMessage().getDocument().getFileId());

                org.telegram.telegrambots.meta.api.objects.File telegramFile =
                        null;
                try {
                    telegramFile = execute(getFileRequest);
                    File file = downloadFile(telegramFile);
                    File file1 = new File("src/main/resources/books/" + books.getId() + ".pdf");
                    if(file.renameTo(file1)){
                        if(file.createNewFile()){
                            books.setUrlToFile(file1.getPath());
                            bookService.updateBook(books);
                        }
                    }
                } catch (TelegramApiException | IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    private void executeEditMessageTextBooksGet(String text, long chatId, long messageId, int page, int count){
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        int c = 0;
        for (int i = page - 3 > 0? page - 3 + 1 : 1; i*5 < count+5; i++){
            if(c <=3) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(Integer.toString(i));
                button.setCallbackData(Integer.toString(i));
                rowInLine.add(button);
                c++;
            }
            else {
                break;
            }
        }
        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("ERROR_TEXT" + e.getMessage());
        }
    }

    private void getBook(String text, Long chatId, int count){
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId.toString());
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        int c = 0;
        for (int i = 1; i*5 < count+5; i++){
            if(c <=3) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(Integer.toString(i));
                button.setCallbackData(Integer.toString(i));
                rowInLine.add(button);
                c++;
            }
            else {
                break;
            }
        }
        rowsInLine.add(rowInLine);
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);
        executeMessage(message);
    }
    private void register(long chatId) {

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register?");

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();

        yesButton.setText("Yes");
        yesButton.setCallbackData("YES_BUTTON");

        var noButton = new InlineKeyboardButton();

        noButton.setText("No");
        noButton.setCallbackData("NO_BUTTON");

        rowInLine.add(yesButton);
        rowInLine.add(noButton);

        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        executeMessage(message);
    }

    private void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("ERROR" + e.getMessage());
        }
    }

    private void executeEditMessageText(String text, long chatId, long messageId){
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("ERROR_TEXT" + e.getMessage());
        }
    }

    public List<String> getReqest(String str){
        List<String> list = new ArrayList<>();
        int start = 0;
        for (int i = 0; i< str.length(); i++){
            if(str.charAt(i) == '='){
                list.add(str.substring(start, i-2));
                start = i-1;
            }
        }
        list.add(str.substring(start));
        return list;
    }

}
