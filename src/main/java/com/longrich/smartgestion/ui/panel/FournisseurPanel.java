package com.longrich.smartgestion.ui.panel;

import javax.swing.JPanel;

import org.springframework.context.annotation.Profile;

import lombok.RequiredArgsConstructor;

@org.springframework.stereotype.Component
@RequiredArgsConstructor
@Profile("!headless")
public class FournisseurPanel extends JPanel{
    
}
