#!/bin/bash

echo "🔧 Compilation du projet SmartGestion..."
./mvnw clean compile -q

if [ $? -eq 0 ]; then
    echo "✅ Compilation réussie!"
    echo ""
    echo "🚀 Lancement du test de validation en temps réel..."
    echo ""
    echo "📝 INSTRUCTIONS IMPORTANTES:"
    echo "========================================="
    echo "1. Une fenêtre va s'ouvrir avec des champs de formulaire"
    echo "2. 🎯 TAPEZ CARACTÈRE PAR CARACTÈRE dans les champs"
    echo "3. Observez la validation EN TEMPS RÉEL pendant que vous tapez:"
    echo ""
    echo "   🔴 ROUGE   : Caractère/séquence invalide"
    echo "   🟠 ORANGE  : En cours (partiellement valide)"  
    echo "   🟢 VERT    : Complètement valide"
    echo "   ⚪ GRIS    : Neutre (vide)"
    echo ""
    echo "🧪 EXEMPLES À TESTER (tapez lentement pour voir l'effet):"
    echo "==========================================================="
    echo "   📞 Téléphone: Tapez '+' puis '2' puis '2' puis '6'..."
    echo "       → +22670123456 ou 70123456"
    echo "   🆔 CNIB: Tapez 'B' puis '1' puis '2'..."
    echo "       → B12345678"
    echo "   🏷️  Code Partenaire: Tapez 'B' puis 'F' puis '1'..."
    echo "       → BF12345678"
    echo "   📦 Code Stockiste: Tapez 'B' puis 'F' puis '1'..."
    echo "       → BF1234"
    echo "   📧 Email: Tapez 't' puis 'e' puis 's' puis 't' puis '@'..."
    echo "       → test@example.com"
    echo ""
    echo "⚡ ASTUCE: Testez des caractères invalides pour voir le rouge !"
    echo ""
    
    # Lancement du test
    java -cp "target/classes" com.longrich.smartgestion.ui.test.ValidationTestMain &
    
    echo "✨ Application lancée ! Regardez la fenêtre qui s'est ouverte."
    echo "🔚 Fermez la fenêtre pour terminer le test."
else
    echo "❌ Erreur de compilation!"
    exit 1
fi