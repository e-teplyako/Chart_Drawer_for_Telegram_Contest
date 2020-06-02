package com.teplyakova.april.telegramcontest.Events;

public interface Publisher {
	void addSubscriber(Subscriber subscriber);
	void removeSubscriber(Subscriber subscriber);
	void notifySubscribers();
}
