import os
import unittest

from classifier.classifier import Classifier

SCRIPT_DIR = os.path.dirname(__file__)

class ClassificationTest(unittest.TestCase):
    def test_cornerstone_scenarios(self):
        """
        Test to classify the cornerstone scenarios.
        """
        scenarios = ["head_on.json",
                     "crossing_giveway.json",
                     "crossing_standon.json",
                     "overtaking_giveway.json",
                     "overtaking_standon.json"]

        os_behaviours = ['alter_course',
                         'alter_course',
                         'keep_course',
                         'alter_course',
                         'keep_course']

        ts_behaviours = ['alter_course',
                         'keep_course',
                         'alter_course',
                         'keep_course',
                         'alter_course']

        for os_behaviour, ts_behaviour, scenario in zip(os_behaviours, ts_behaviours, scenarios):
            with self.subTest(i=scenario):
                situation = SCRIPT_DIR + '/../../scenarios/' + scenario

                classifier = Classifier()
                result = classifier.classify(situation)

                self.assertEqual(result['own-ship']['behaviour'], os_behaviour)
                self.assertEqual(result['target-ship']['behaviour'], ts_behaviour)

    @unittest.skip("Scenario files not ready yet")
    def test_sailing_vessel_scenarios(self):
        """
        Test to classify sailing vessel encounters.
        """
        scenarios = ["sailing_different_side_giveway.json",
                     "sailing_different_side_standon.json",
                     "sailing_same_side_giveway.json",
                     "sailing_same_side_standon.json"]

        os_behaviours = ['alter_course',
                         'keep_course',
                         'alter_course',
                         'keep_course']

        ts_behaviours = ['keep_course',
                         'alter_course',
                         'keep_course',
                         'alter_course']

        for os_behaviour, ts_behaviour, scenario in zip(os_behaviours, ts_behaviours, scenarios):
            situation = SCRIPT_DIR + '/../../scenarios/' + scenario

            classifier = Classifier()
            result = classifier.classify(situation)

            self.assertEqual(result['own-ship']['behaviour'], os_behaviour)
            self.assertEqual(result['target-ship']['behaviour'], ts_behaviour)

    @unittest.skip("Scenario files not ready yet")
    def test_different_vessel_scenarios(self):
        """
        Test to classify different vessel encounter.
        """
        scenarios = ["diff_pdv_sv.json",
                     "diff_pdv_veif.json",
                     "diff_pdv_vrihatm.json",
                     "diff_pdv_vcbhd.json",
                     "diff_pdv_vnuc.json",
                     "diff_sv_veif.json",
                     "diff_sv_vrihatm.json",
                     "diff_sv_vcbhd.json",
                     "diff_sv_vnuc.json",
                     "diff_veif_vrihatm.json",
                     "diff_veif_vcbhd.json",
                     "diff_veif_vnuc.json"]

        for scenario in scenarios:
            situation = SCRIPT_DIR + '/../../scenarios/' + scenario

            classifier = Classifier()
            result = classifier.classify(situation)

            self.assertEqual(result['own-ship']['behaviour'], 'alter_course')
            self.assertEqual(result['target-ship']['behaviour'], 'keep_course')

