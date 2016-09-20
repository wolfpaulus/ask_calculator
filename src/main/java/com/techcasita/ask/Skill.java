package com.techcasita.ask;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.speechlet.servlet.SpeechletServlet;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * The Skill class is the core servlet mapped in web.xml to respond to https://server/{war}/{name}
 * On every incoming request, the echo-user's unique id (session.getUser().getUserId()) is provided.
 * The Skill class will instantiate an object to that can handle the identified intent.
 *
 * @author <a href="mailto:wolf@wolfpaulus.com">Wolf Paulus</a>
 */
public class Skill extends SpeechletServlet implements Speechlet {
    private static final Logger Log = Logger.getLogger(Skill.class);

    public Skill() {
        this.setSpeechlet(this);
    }

    private static String calc(final int op1, final int op2, final String operator) {
        if ("plus".equals(operator)) {
            return String.format("%d plus %d equals %d", op1, op2, op1 + op2);
        } else if ("minus".equals(operator)) {
            return String.format("%d minus %d equals %d", op1, op2, op1 - op2);
        } else if ("times".equals(operator) || "multiplied by".equals(operator)) {
            return String.format("%d times %d equals %d", op1, op2, op1 * op2);
        } else if ("divided by".equals(operator)) {
            String mask = "%d divided by %d equals %d";
            if ((op1 % op2) > 0) {
                mask += ", the remainder is %d";
            }
            return String.format(mask, op1, op2, op1 / op2, op1 % op2);
        }
        return "I did not understand the operation you want me to perform";
    }

    private static boolean isPrime(final int n) {
        if (n == 2 || n == 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }

        int i = 5;
        int w = 2;

        while (i * i <= n) {
            if (n % i == 0) {
                return false;
            }
            i += w;
            w = 6 - w;
        }
        return true;
    }


    @Override
    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        Log.info("Session Started for " + session.getUser().getUserId());
    }

    @Override
    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        Log.info("onLaunch called for " + session.getUser().getUserId());

        final SpeechletResponse response = new SpeechletResponse();
        final PlainTextOutputSpeech speech1 = new PlainTextOutputSpeech();
        final PlainTextOutputSpeech speech2 = new PlainTextOutputSpeech();
        final Reprompt reprompt = new Reprompt();

        speech1.setText("Hello, I am your Calculator");
        speech2.setText("I can perform simple Addition and Subtraction, and also Multiplication and Division.");
        reprompt.setOutputSpeech(speech2);
        response.setOutputSpeech(speech1);
        response.setReprompt(reprompt);
        response.setShouldEndSession(false);

        return response;
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        Log.info("onIntent called for " + session.getUser().getUserId());

        final SpeechletResponse response = new SpeechletResponse();
        final PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("I don't know what you are talking about.");

        if (request != null && request.getIntent() != null) {
            Intent intent = request.getIntent();
            if (intent.getName().equals("Calculate")) {
                final Map<String, Slot> map = intent.getSlots();
                if (map.containsKey("operatorA") && map.containsKey("operatorB") && map.containsKey("operation")) {
                    int a = Integer.valueOf(map.get("operatorA").getValue());
                    int b = Integer.valueOf(map.get("operatorB").getValue());
                    speech.setText(calc(a, b, map.get("operation").getValue()));
                }
            } else if (intent.getName().equals("CheckPrime")) {
                final Map<String, Slot> map = intent.getSlots();
                if (map.containsKey("operator")) {
                    final int k = Integer.valueOf(map.get("operator").getValue());
                    final String s = isPrime(k) ? "" : "not";
                    speech.setText(String.format("%d is %s a Prime number", k, s));
                }
            }
        }
        response.setOutputSpeech(speech);
        response.setShouldEndSession(true);
        return response;
    }

    @Override
    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        Log.info("Session Ended for " + session.getUser().getUserId());
    }
}