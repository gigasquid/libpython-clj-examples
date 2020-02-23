(ns gigasquid.psutil.core
  (:require [libpython-clj.require
             :refer [require-python]]
            [libpython-clj.python
             :as py
             :refer [py.
                     py..
                     py.-
                     att-type-map
                     ->python
                     ->jvm]]
            [clojure.java.shell :as sh]
            [clojure.string :as str]
            [clojure.pprint :refer [pprint]])
  (:import [java.io File]))

;; https://psutil.readthedocs.io/en/latest/
;; https://psutil.readthedocs.io/en/latest/#filtering-and-sorting-processes

;; Python
;; pip3 install pyutil

(require-python '[builtins :as python])
(require-python '[psutil :as psu])

(comment
  ;; Get information about the process
  (def p (psu/Process))

  (py.. p username)
  ;;=> "bchoomnuan"

  (py.. p cpu_times)
  ;;=> pcputimes(user=52.755533824, system=1.542032128, children_user=0.0, children_system=0.0)

  ;; And the list goes on
  (py.. p
        ;;cpu_percent
        ;;create_time
        ;;ppid
        ;;status
        ;;cmdline ;; intersting one!
        ;;as_dict
        ;;parents
        ;;cwd ;;=> "/Users/bchoomnuan/github/libpython-clj-examples"
        ;;uids
        ;;gids
        ;;terminal
        ;;memory_info
        ;;memory_full_info
        ;;(memory_percent :memtype "rss")

        ;; More fun to try these [I will skip this for now :)]
        ;; (send_signal ..)
        ;; (suspend)
        ;; (resume)
        ;; (terminate)
        ;; (kill)
        ;; (wait :timeout ..)
        )
  )

(comment
  ;; List all running process name
  (def process-names
    (let [procs (psu/process_iter["name"])]
      (map (fn [p]
             (py. p name)) procs)))

  (count process-names) ;;=> 475

  ;; Take a peek at the first few items
  (take 5 process-names)
  ;;=> ("kernel_task" "launchd" "syslogd" "UserEventAgent" "uninstalld")

  ;; How about finding out all process that have the word "sys" in it?

  (filter (fn [x] (str/index-of (str/lower-case x) "sys"))
          process-names)

  ;;=>
  #_
  ("syslogd" "systemstats" "syspolicyd" "sysmond" "systemstats" "systemsoundserve" "UIKitSystem" "SystemUIServer" "system_installd" "sysextd" "sysdiagnose")

  ;; You can do more of course, see the documentation for idea
  ;; https://psutil.readthedocs.io/en/latest/#filtering-and-sorting-processes

  )

(comment
  ;; There are many functions that we can use like getting information about cpu
  (def cpu-times (psu/cpu_times))

  cpu-times ;;=> scputimes(user=67053.19, nice=0.0, system=52277.51, idle=1399764.29)

  ;; Note: the return is the Pythong object that we can inspect like
  (py.- cpu-times user)
  (py.- cpu-times system)
  (py.- cpu-times idle)

  ;; How about printing out the percentage of cpu usage every given interval
  (dotimes [x 3]
    (println (psu/cpu_percent :interval 1)))

  ;;=> ;; in your REPL
  ;; 3.4
  ;; 3.5
  ;; 3.4

  (def cpu-info
    (for [x (range 3)]
      (psu/cpu_percent :interval 1
                       :percpu true)))

  (type cpu-info)
  ;;=> clojure.lang.LazySeq

  (pprint cpu-info)
  ;;=> ;; in your REPL
  #_
  ([57.4, 1.0, 18.0, 0.0, 10.9, 0.0, 5.1, 0.0, 6.1, 0.0, 4.0, 0.0]
   [60.0, 0.0, 17.8, 1.0, 8.0, 1.0, 5.9, 0.0, 5.0, 0.0, 4.0, 0.0]
   [57.0, 1.0, 24.8, 3.0, 11.9, 0.0, 7.1, 0.0, 5.0, 0.0, 2.0, 0.0])

  (-> cpu-info first type)
  ;;=> :pyobject

  (-> cpu-info first first type) ;;=> java.lang.Double

  (def cpu-info
    (for [x (range 3)]
      (psu/cpu_times_percent :interval 1
                             :percpu false)))

  (-> cpu-info
      pprint)

  ;;=> in your REPL
  ;; (scputimes(user=1.5, nice=0.0, system=2.0, idle=96.5)
  ;;           scputimes(user=1.0, nice=0.0, system=2.8, idle=96.2)
  ;;           scputimes(user=0.7, nice=0.0, system=1.8, idle=97.4))

  (psu/cpu_count) ;;=> 12

  (psu/cpu_count :logical false) ;;=> 6

  (psu/cpu_stats)
  ;;=> scpustats(ctx_switches=148596, interrupts=866048, soft_interrupts=579676465, syscalls=1635282)

  (psu/cpu_freq)
  ;;=> scpufreq(current=2200, min=2200, max=2200)

  (psu/getloadavg)
  ;;=> (3.3349609375, 2.94970703125, 2.6689453125)
  )

;; Memory
(comment

  (psu/virtual_memory)
  ;;=>  svmem(total=17179869184, available=7311126528, percent=57.4, used=8922701824, free=184188928, active=5441355776, inactive=6698086400, wired=3481346048)

  (psu/swap_memory)
  ;;=> sswap(total=3221225472, used=1549008896, free=1672216576, percent=48.1, sin=206163009536, sout=310902784)

  ;; Disks
  (psu/disk_partitions)

  #_ [sdiskpart(device='/dev/disk1s6', mountpoint='/', fstype='apfs', opts='ro,local,rootfs,dovolfs,journaled,multilabel'), sdiskpart(device='/dev/disk1s1', mountpoint='/System/Volumes/Data', fstype='apfs', opts='rw,local,dovolfs,dontbrowse,journaled,multilabel'), sdiskpart(device='/dev/disk1s4', mountpoint='/private/var/vm', fstype='apfs', opts='rw,local,dovolfs,dontbrowse,journaled,multilabel'), sdiskpart(device='/dev/disk1s5', mountpoint='/Volumes/Macintosh HD', fstype='apfs', opts='rw,local,dovolfs,journaled,multilabel'), sdiskpart(device='/dev/disk1s3', mountpoint='/Volumes/Recovery', fstype='apfs', opts='rw,local,dovolfs,dontbrowse,journaled,multilabel')]

  (def du (psu/disk_usage "/"))

  ;;=> sdiskusage(total=250685575168, used=10963034112, free=7645040640, percent=58.9)

  (py.- du total)
  (py.- du used)
  (py.- du percent)

  (psu/disk_io_counters :perdisk false)
  ;;=> sdiskio(read_count=15153434, write_count=5535766, read_bytes=278249762816, write_bytes=100455395328, read_time=6554143, write_time=2819768)

  ;; Network
  (psu/net_io_counters :pernic true)

  (psu/net_if_addrs)

  (psu/net_if_stats)

  )

(comment
  ;; Sensors!
  (psu/sensors_battery)
  ;;=> sbattery(percent=97, secsleft=23400, power_plugged=False)

  (psu/swap_memory)
  ;;=> sswap(total=3221225472, used=1414791168, free=1806434304, percent=43.9, sin=219395137536, sout=312922112)

  ;; Others
  (psu/users)
  ;;=> [suser(name='bchoomnuan', terminal='console', host=None, started=1582210432.0, pid=199), suser(name='bchoomnuan', terminal='ttys001', host=None, started=1582428288.0, pid=24455)]

  )

(comment
  ;; Process management
  (count (psu/pids)) ;;=> 501

  (last (psu/pids))

  ;; Take random process object
  (def p (psu/Process (last (psu/pids))))

  (py. p name) ;;=> "microstackshot"

  (py. p exe)  ;;=> "/usr/libexec/microstackshot"
  (py. p ppid) ;;=> 1

  (py.. p (children :recursive true))

  )

(comment

  ;; Take a peek at 5 processes
  (doseq [proc (take 5 (psu/process_iter ["pid" "name"]))]
    (println proc)
    )

  ;;=> Your REPL
  ;; psutil.Process(pid=0, name='kernel_task', started='2020-02-20 09:53:34')
  ;; psutil.Process(pid=1, name='launchd', started='2020-02-20 09:53:34')
  ;; psutil.Process(pid=120, name='syslogd', started='2020-02-20 09:53:41')
  ;; psutil.Process(pid=121, name='UserEventAgent', started='2020-02-20 09:53:41')
  ;; psutil.Process(pid=124, name='uninstalld', started='2020-02-20 09:53:41')

  (psu/pid_exists 99532) ;;=> true

  )

;; There are much more things you can do, just go ahead and looking at the
;; official documentation to see nice and practical usage of the library.
;; https://psutil.readthedocs.io/en/latest/#
