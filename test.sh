for file in ./src/test/resources/scenarios/*;
do
    ./colreg-classifier.sh -f "${file}" -o "./src/test/resources/classification-results/${file}-result.json";
done
